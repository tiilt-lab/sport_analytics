
import os
import csv
import time
# import argparse
from statistics import mean, mode

# lstm model
from numpy import std
from numpy import dstack
from pandas import read_csv
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import Dropout
from keras.layers import LSTM
from keras.utils import to_categorical
import tensorflow as tf

global graph
from flask import Flask, render_template
from flask_socketio import SocketIO, emit

graph = tf.get_default_graph()
app = Flask(__name__)
socketio = SocketIO(app)

class LSTMModel():

    def make_csvs(self, filenames, datasets, rows=False):
        for j in range(4):
            with open(filenames[j], "w+") as my_csv:
                csvWriter = csv.writer(my_csv, delimiter=' ')
                if rows:
                    csvWriter.writerows(datasets[j])
                else:
                    csvWriter.writerow(datasets[j])

    def generate_files(self, window_size, overlap_size, training_split, save_windows, save_streams):
        X, Y, Z, actv = [], [], [], []

        # pull data from file, split into lists
        with open('./DIMDataset/clean_combined.csv', 'r') as f:
            for line in f:
                line_list = line.split(',')

                X.append(int(line_list[0]))
                Y.append(int(line_list[1]))
                Z.append(int(line_list[2]))
                actv.append(int(line_list[3]))
            print('Samples:', len(X))

        # write streams to file
        stream_filenames = [
            './DIMDataset/stream_x.csv', './DIMDataset/stream_y.csv',
            './DIMDataset/stream_z.csv', './DIMDataset/stream_actv.csv']
        datasets = [X, Y, Z, actv]
        if save_streams:
            self.make_csvs(stream_filenames, datasets)

        ###########################################################################
        # group streams into windows
        # window_size = 64 # 64 = 1sec windows
        # overlap_size = 16

        X_windows = [X[x:x + window_size] for x in range(0, len(X), window_size - overlap_size)]
        Y_windows = [Y[x:x + window_size] for x in range(0, len(Y), window_size - overlap_size)]
        Z_windows = [Z[x:x + window_size] for x in range(0, len(Z), window_size - overlap_size)]
        actv_windows = [actv[x:x + window_size] for x in range(0, len(actv), window_size - overlap_size)]

        # normalize last chunk with mean values
        for item in [X_windows, Y_windows, Z_windows, actv_windows]:
            if len(item[-1]) < window_size:
                item[-1] = item[-1] + [int(mean(item[-1]))] * (window_size - len(item[-1]))
        print('Windows:', len(X_windows))

        for ii in range(len(actv_windows)):
            try:
                actv_windows[ii] = [mode(actv_windows[ii])]
            except:
                actv_windows[ii] = [int(round(mean(actv_windows[ii])))]

        # write windows to file
        window_filenames = ['./DIMDataset/windows_x.csv', './DIMDataset/windows_y.csv',
                            './DIMDataset/windows_z.csv', './DIMDataset/windows_actv.csv']
        datasets = [X_windows, Y_windows, Z_windows, actv_windows]
        if save_windows:
            self.make_csvs(window_filenames, datasets, rows=True)

        ################################################################################################

        marker = int(len(X_windows) * training_split)
        train_filenames = ['./DIMDataset/train/empatica/acc_x_train.csv',
                           './DIMDataset/train/empatica/acc_y_train.csv',
                           './DIMDataset/train/empatica/acc_z_train.csv',
                           './DIMDataset/train/actv_train.csv']

        test_filenames = ['./DIMDataset/test/empatica/acc_x_test.csv',
                          './DIMDataset/test/empatica/acc_y_test.csv',
                          './DIMDataset/test/empatica/acc_z_test.csv',
                          './DIMDataset/test/actv_test.csv']

        os.makedirs('./DIMDataset/train/empatica/', exist_ok=True)
        os.makedirs('./DIMDataset/test/empatica/', exist_ok=True)

        for i in range(len(datasets)):
            train = datasets[i][:marker]
            test = datasets[i][marker:]

            with open(train_filenames[i], 'w+') as train_f:
                csvWriter = csv.writer(train_f, delimiter=' ')
                csvWriter.writerows(train)

            with open(test_filenames[i], 'w+') as test_f:
                csvWriter = csv.writer(test_f, delimiter=' ')
                csvWriter.writerows(test)

    # load a single file as a numpy array
    def load_file(self, filepath):
        dataframe = read_csv(filepath, header=None, delim_whitespace=True)
        return dataframe.values

    # load a list of files and return as a 3d numpy array
    def load_group(self, filenames, prefix=''):
        loaded = list()
        for name in filenames:
            data = self.load_file(prefix + name)
            loaded.append(data)
        # stack group so that features are the 3rd dimension
        loaded = dstack(loaded)
        return loaded

    # load a dataset group, such as train or test
    def load_dataset_group(self, group, prefix=''):
        filepath = prefix + group + '/empatica/'
        # load all 9 files as a single array
        filenames = list()
        # total acceleration
        filenames += ['acc_x_' + group + '.csv', 'acc_y_' + group + '.csv', 'acc_z_' + group + '.csv']
        # load input data
        X = self.load_group(filenames, filepath)
        # load class output
        y = self.load_file(prefix + group + '/actv_' + group + '.csv')
        return X, y

    # load the dataset, returns train and test X and y elements
    def load_dataset(self, prefix=''):
        # load all train
        trainX, trainy = self.load_dataset_group('train', prefix + 'DIMDataset/')
        # load all test
        testX, testy = self.load_dataset_group('test', prefix + 'DIMDataset/')
        # zero-offset class values
        trainy = trainy - 1
        testy = testy - 1
        # one hot encode y
        trainy = to_categorical(trainy)
        testy = to_categorical(testy)
        return trainX, trainy, testX, testy

    # fit and evaluate a model
    def evaluate_model(self, trainX, trainy, testX, testy):
        verbose, epochs, batch_size = 0, 15, 64
        n_timesteps, n_features, n_outputs = trainX.shape[1], trainX.shape[2], trainy.shape[1]
        model = Sequential()
        model.add(LSTM(100, input_shape=(n_timesteps, n_features)))
        model.add(Dropout(0.5))
        model.add(Dense(100, activation='relu'))
        model.add(Dense(n_outputs, activation='softmax'))
        model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

        # serialize model to JSON
        model_json = model.to_json()
        with open("model.json", "w") as json_file:
            json_file.write(model_json)

        # serialize weights to HDF5
        model.save_weights("model.h5")
        print("Saved model to disk")

        # fit network
        model.fit(trainX, trainy, epochs=epochs, batch_size=batch_size, verbose=verbose)
        # evaluate model
        _, accuracy = model.evaluate(testX, testy, batch_size=batch_size, verbose=0)
        return accuracy

    # summarize scores
    def summarize_results(self, scores):
        # print(scores)
        m, s = mean(scores), std(scores)
        return m

    # run an experiment
    def run_experiment(self, repeats=1):
        # load data
        trainX, trainy, testX, testy = self.load_dataset()
        # repeat experiment
        scores = list()
        for r in range(repeats):
            score = self.evaluate_model(trainX, trainy, testX, testy)
            score = score * 100.0
            scores.append(score)
        # summarize results
        return self.summarize_results(scores)

    def train(self, window_size, overlap_size, training_split,
              save_windows=False, save_streams=False):
        # run the experiment
        start_time = time.time()
        self.generate_files(window_size, int(window_size * overlap_size), training_split,
                            save_windows, save_streams)
        res = self.run_experiment()

        print('\n', 'Mean Accuracy:', res)
        print("total time elapsed", int((time.time() - start_time) / 60), " min")
        return 'Mean Accuracy: {}'.format(str(res))

    def run_sweep(self):
        # sweep the parameters
        windows = [16, 32, 48, 64, 80, 96, 112, 128]
        overlap = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
        training = [0.6, 0.7, 0.8, 0.9]

        start_time = time.time()
        with open('results.csv', 'w+') as f:
            for i in windows:
                for j in overlap:
                    for k in training:
                        print(' '.join([str(i), str(int(j * i)), str(k)]))

                        self.generate_files(i, int(j * i), k)
                        res = self.run_experiment()

                        f.write(' '.join([str(i), str(j), str(k), str(res), '\n']))
                        print('acc:', res)
                        print('time:', int((time.time() - start_time) / 60), " min")
                        print()
        print("total time elapsed", int((time.time() - start_time) / 60), " min")

ids = { }


@app.route('/')
@app.route('/home')
def index():
    # out = train(16, 0.5, 0.6, False, False)
    # return "<h1>" + out + "</h1>"
    return "<p>Hello!</p> <a href=\"/train\">Train Model</a>"


@app.route('/train')
def train_model():
    output = ""
    with graph.as_default():
        my_model = LSTMModel()
        out = my_model.train(16, 0.5, 0.6, False, False)
        output += "<h1>" + out + "</h1>"
        output += "<a href=\"/\">Home</a>"

        return output


@app.route('/store/<data>')
def store(data):
    print(data)

    with open('new_data.csv', 'a+') as f:
        data_list = data.split(',')
        csvWriter = csv.writer(f, delimiter=',')
        csvWriter.writerow(data_list)

    return str(data_list)


# @socketio.on('echo', namespace='/ws')
# def test_message(message):
#     emit('my response', {'data': message['ws']})


# @socketio.on('broadcast', namespace='/ws')
# def test_message(message):
#     emit('my response', {'data': message['data']}, broadcast=True)


# @socketio.on('connect', namespace='/ws')
# def test_connect():
#     emit('my response', {'data': 'Connected'})


# @socketio.on('disconnect', namespace='/ws')
# def test_disconnect():
#     print('Client disconnected')


if __name__ == '__main__':
    # app.run(reload=True)
    app.run(debug=True, host='0.0.0.0')
    # socketio.run(app)
