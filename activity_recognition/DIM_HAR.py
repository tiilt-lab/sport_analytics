import os
import csv
import time
import argparse
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


def make_csvs(filenames, datasets, rows=False):
    for j in range(4):
        with open(filenames[j], "w+") as my_csv:
            csvWriter = csv.writer(my_csv, delimiter=' ')
            if rows:
                csvWriter.writerows(datasets[j])
            else:
                csvWriter.writerow(datasets[j])


def generate_files(window_size, overlap_size, training_split, save_windows, save_streams):
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
        make_csvs(stream_filenames, datasets)

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
            item[-1] = item[-1] + [int(mean(item[-1]))] * (window_size-len(item[-1]))
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
        make_csvs(window_filenames, datasets, rows=True)

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
            csvWriter = csv.writer(train_f,delimiter=' ')
            csvWriter.writerows(train)

        with open(test_filenames[i], 'w+') as test_f:
            csvWriter = csv.writer(test_f,delimiter=' ')
            csvWriter.writerows(test)


# load a single file as a numpy array
def load_file(filepath):
    dataframe = read_csv(filepath, header=None, delim_whitespace=True)
    return dataframe.values


# load a list of files and return as a 3d numpy array
def load_group(filenames, prefix=''):
    loaded = list()
    for name in filenames:
        data = load_file(prefix + name)
        loaded.append(data)
    # stack group so that features are the 3rd dimension
    loaded = dstack(loaded)
    return loaded


# load a dataset group, such as train or test
def load_dataset_group(group, prefix=''):
    filepath = prefix + group + '/empatica/'
    # load all 9 files as a single array
    filenames = list()
    # total acceleration
    filenames += ['acc_x_'+group+'.csv', 'acc_y_'+group+'.csv', 'acc_z_'+group+'.csv']
    # load input data
    X = load_group(filenames, filepath)
    # load class output
    y = load_file(prefix + group + '/actv_'+group+'.csv')
    return X, y


# load the dataset, returns train and test X and y elements
def load_dataset(prefix=''):
    # load all train
    trainX, trainy = load_dataset_group('train', prefix + 'DIMDataset/')
    # load all test
    testX, testy = load_dataset_group('test', prefix + 'DIMDataset/')
    # zero-offset class values
    trainy = trainy - 1
    testy = testy - 1
    # one hot encode y
    trainy = to_categorical(trainy)
    testy = to_categorical(testy)
    return trainX, trainy, testX, testy


# fit and evaluate a model
def evaluate_model(trainX, trainy, testX, testy):
    verbose, epochs, batch_size = 0, 15, 64
    n_timesteps, n_features, n_outputs = trainX.shape[1], trainX.shape[2], trainy.shape[1]
    model = Sequential()
    model.add(LSTM(100, input_shape=(n_timesteps,n_features)))
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
def summarize_results(scores):
    #print(scores)
    m, s = mean(scores), std(scores)
    return m


# run an experiment
def run_experiment(repeats=1):
    # load data
    trainX, trainy, testX, testy = load_dataset()
    # repeat experiment
    scores = list()
    for r in range(repeats):
        score = evaluate_model(trainX, trainy, testX, testy)
        score = score * 100.0
        scores.append(score)
    # summarize results
    return summarize_results(scores)


def train(window_size, overlap_size, training_split,
          save_windows=False, save_streams=False):
    # run the experiment
    start_time = time.time()
    generate_files(window_size, int(window_size * overlap_size), training_split,
                   save_windows, save_streams)
    res = run_experiment()

    print('\n','Mean Accuracy:', res)
    print("total time elapsed", int((time.time() - start_time) / 60), " min")


def run_sweep():
    # sweep the parameters
    windows = [16, 32, 48, 64, 80, 96, 112, 128]
    overlap = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
    training = [0.6, 0.7, 0.8, 0.9]

    start_time = time.time()
    with open('results.csv', 'w+') as f:
        for i in windows:
            for j in overlap:
                for k in training:
                    print(' '.join([str(i), str(int(j*i)), str(k)]))

                    generate_files(i, int(j*i), k)
                    res = run_experiment()

                    f.write(' '.join([str(i), str(j), str(k), str(res), '\n']))
                    print('acc:', res)
                    print('time:', int((time.time() - start_time) / 60), " min")
                    print()
    print("total time elapsed", int((time.time() - start_time) / 60), " min")


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    # window_size, overlap_size, training_split
    parser.add_argument("window_size", type=int,
                        help="number of samples per window, 64 = 1sec")
    parser.add_argument("overlap_size", type=float,
                        help="percent of overlap between windows")
    parser.add_argument("training_split", type=float,
                        help="percent of data used for training")

    parser.add_argument("-w", "--windows", action="store_true",
                        help="write windowed data to file")
    parser.add_argument("-s", "--streams", action="store_true",
                        help="write data streams to file")
    args = parser.parse_args()

    train(args.window_size, args.overlap_size, args.training_split,
          args.windows, args.streams)
    # train(16, 0.5, 0.6)
    # run_sweep()
