import pandas as pd
import numpy as np
from sklearn.feature_selection import SelectKBest
from sklearn.feature_selection import chi2

data = pd.read_csv("/home/subhranil/Desktop/parkinson_dataset.csv")
max(data['motor_UPDRS'])
min(data['motor_UPDRS'])
max(data['total_UPDRS'])
min(data['total_UPDRS'])

X = data.iloc[:,6:22]  #independent columns
y = data.iloc[:, 4]    #target column i.e price range
y1 = data.iloc[:, 5]

output1=[]
output2=[]
for i in data['motor_UPDRS']:
    if i>=0 and i<=20:
        output1.append("1")
    elif i>20 and i<40:
        output1.append("2")
    """elif i>=20 and i<30:
        output1.append("3")
    elif i>=30 and i<40:
        output1.append("4")"""
      
for i in data['total_UPDRS']:
    if i>=0 and i<=25:
        output2.append("1")
    elif i>25 and i<55:
        output2.append("2")
    """elif i>=25 and i<40:
        output2.append("3")
    elif i>=40 and i<55:
        output2.append("4")"""
        

#apply SelectKBest class to extract top 10 best features
bestfeatures = SelectKBest(score_func=chi2, k=10)
fit = bestfeatures.fit(X,output2)
dfscores = pd.DataFrame(fit.scores_)
dfcolumns = pd.DataFrame(X.columns)
#concat two dataframes for better visualization 
featureScores = pd.concat([dfcolumns,dfscores],axis=1)
featureScores.columns = ['Specs','Score']  #naming the dataframe columns
print(featureScores.nlargest(10,'Score'))

from sklearn.ensemble import ExtraTreesClassifier
import matplotlib.pyplot as plt
model = ExtraTreesClassifier()
model.fit(X,output2)
print(model.feature_importances_) #use inbuilt class feature_importances of tree based classifiers
#plot graph of feature importances for better visualization
feat_importances = pd.Series(model.feature_importances_, index=X.columns)
feat_importances.nlargest(10).plot(kind='barh')
plt.show()