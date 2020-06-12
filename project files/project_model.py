#importing libraries
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
 
#importing dataset
dataset = pd.read_csv("parkinson_dataset_1.csv")

#feature matrix creation
X = dataset.iloc[:, 6:21].values
Y = dataset.iloc[:, [4,5]].values                 

#creation of train and test data
from sklearn.model_selection import train_test_split
X_train, X_test, Y_train, Y_test = train_test_split(X,Y,test_size = 0.1, random_state = 0)

#standardisation
#from sklearn.preprocessing import StandardScaler
from sklearn.preprocessing import MinMaxScaler
sc = MinMaxScaler()
X_train = sc.fit_transform(X_train)
X_test = sc.transform(X_test)
Y_train = sc.fit_transform(Y_train)
Y_test = sc.transform(Y_test)

import keras
from keras.models import Sequential
from keras.layers import Dense
from keras.callbacks import ModelCheckpoint
from keras.models import load_model
from keras.callbacks import LearningRateScheduler

def lr_schedule(epoch):
    lr = 1e-3
    if epoch > 180:
        lr *= 1e-4
    elif epoch > 160:
        lr *= 1e-3
    elif epoch > 120:
        lr *= 1e-2
    elif epoch > 40:
        lr *= 1e-1

    return lr

lr_scheduler = LearningRateScheduler(lr_schedule)

def neural_net(lossfunc, bat_size, ep_size, OPTIMIZER):
    classifier = Sequential()
    classifier.add(Dense(units = 100,kernel_initializer = 'normal',activation = 'relu',input_dim = 15))
    classifier.add(Dense(units = 200,kernel_initializer = 'normal',activation = 'relu'))
    classifier.add(Dense(units = 100,kernel_initializer = 'normal',activation = 'relu'))
    classifier.add(Dense(units = 2,kernel_initializer = 'normal',activation = 'linear'))
    
    classifier.compile(optimizer = OPTIMIZER , loss = lossfunc , metrics = ['mse'])
    classifier.summary()
    
    filepath="/home/subhranil/Train_Models/weights-improvement-{epoch:02d}-{loss:.4f}.hdf5"
    checkpoint = ModelCheckpoint(filepath, monitor='loss', verbose=1, save_best_only=True,save_weights_only=False)
    callbacks_list = [checkpoint]

    history1 = classifier.fit(X_train,Y_train,batch_size = bat_size , nb_epoch = ep_size,callbacks = callbacks_list,verbose=1)
    #history2 = classifier.fit(X_test,Y_test,batch_size = bat_size , nb_epoch = ep_size, verbose=2)
    
    return history1,classifier

ADAM = keras.optimizers.Adam()
#ADAGRAD = keras.optimizers.Adagrad()
#NADAM = keras.optimizers.Nadam()
#SGD = keras.optimizers.SGD()
#ADADELTA = keras.optimizers.Adadelta()
#RMSPROP = keras.optimizers.RMSprop()
#ADAMAX = keras.optimizers.Adamax()

train_history_mse , classifier = neural_net('mean_squared_error',20,1000,ADAM)
#train_history_msle , classifier , test_history_msle = neural_net('mean_squared_logarithmic_error',20,1000,ADAM)
#train_history_mae , classifier , test_history_mae = neural_net('mean_absolute_error',20,1000,ADAM)
 
train_mse = classifier.evaluate(X_train, Y_train, verbose=0)
test_mse = classifier.evaluate(X_test, Y_test, verbose=0)
print('Train: %.5f, Test: %.5f' % (train_mse[0], test_mse[0]))
print('Train: %.5f, Test: %.5f' % (train_mse[1], test_mse[1]))


best_model = load_model('/home/subhranil/Train_Models/weights-improvement-970-0.0025.hdf5')

#testing data prediction
Y_pred = best_model.predict(X_test[10:11])
Y_pred_org = sc.inverse_transform(Y_pred)
Y_test_org = sc.inverse_transform(Y_test)

Y_pred[:, 0] = (Y_pred_org[:, 0] > 20)
Y_pred[:, 1] = (Y_pred_org[:, 1] > 25)
Y_test[:, 0] = (Y_test_org[:, 0] > 20)
Y_test[:, 1] = (Y_test_org[:, 1] > 25)

from sklearn.metrics import confusion_matrix
cm_0 = confusion_matrix(Y_test[:, 0],Y_pred[:, 0])
cm_1 = confusion_matrix(Y_test[:, 1],Y_pred[:, 1])

#training data prediction
Y_pred_1 = best_model.predict(X_train)
Y_pred_org_1 = sc.inverse_transform(Y_pred_1)
Y_test_org_1 = sc.inverse_transform(Y_train)

Y_pred_1[:, 0] = (Y_pred_org_1[:, 0] > 20)
Y_pred_1[:, 1] = (Y_pred_org_1[:, 1] > 25)
Y_train[:, 0] = (Y_test_org_1[:, 0] > 20)
Y_train[:, 1] = (Y_test_org_1[:, 1] > 25)

cm_2 = confusion_matrix(Y_train[:, 0],Y_pred_1[:, 0])
cm_3 = confusion_matrix(Y_train[:, 1],Y_pred_1[:, 1])


def loss_plot(P,Q,X,Y):
    plt.title('Loss Functions')
    plt.plot(P.history['loss'], label=X)
    plt.plot(Q.history['loss'], label=Y)
    plt.xlabel('No of Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.show()
    
loss_plot(train_history_mse,test_history_mse,'Train-MSE','Test-MSE')
loss_plot(train_history_msle,test_history_msle,'Train-MSLE','Test-MSLE')
loss_plot(train_history_mae,test_history_mae,'Train-MAE','Test-MAE')
