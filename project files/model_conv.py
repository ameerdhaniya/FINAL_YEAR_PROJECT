import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow import lite

keras_file = "/home/subhranil/Train_Models/weights-improvement-992-0.1720.hdf5"
converter = lite.TocoConverter.from_keras_model_file(keras_file)
tflite_model = converter.convert()
open("demo.tflite","wb").write(tflite_model)
