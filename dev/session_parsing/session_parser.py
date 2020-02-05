
import numpy as np
# import plotly.express as px


def get_data_object(fname='./Session_1-13-20.txt'):
    """
    data_object
        user_id/
            dtype/
                x/
                    [...]
                y/
                    [...]
                z/
                    [...]
            dtype/
                ...
        user_id/
        ...
    """

    data = {}

    # open the session file by name
    with open(fname, 'r+') as f:
        # iterate over each line of th text file
        for line in f:
            # split on commas, into a list of strings
            line_info = line.split(',')
            # ignore blank lines
            if len(line_info) > 1:
                # if its a new id, make a place for it in the dict
                if line_info[1] not in list(data.keys()):
                    data[line_info[1]] = {}
                # its a new data type
                if line_info[2] not in list(data[line_info[1]].keys()):
                    data[line_info[1]][line_info[2]] = {'x': [],
                                                        'y': [],
                                                        'z': []}

                data[line_info[1]][line_info[2]]['x'].append(float(line_info[3]))
                data[line_info[1]][line_info[2]]['y'].append(float(line_info[4]))
                data[line_info[1]][line_info[2]]['z'].append(float(line_info[5]))

    for key in list(data.keys()):
        print("ID: ", key)
        for dtype in list(data[key].keys()):
            print("   ", dtype, ": ", len(data[key][dtype]['x']))
    # print(data[list(data.keys())[1]].keys())
    # print(data[list(data.keys())[0]]['Accelerometer']['x'])
    return data


def make_plot(sample, id='b80d174ba44f5fb1', dtype='Accelerometer'):
    sample_len = len(sample[id][dtype]['x'])

    x = np.arange(sample_len)
    xvals = sample[id][dtype]['x']
    yvals = sample[id][dtype]['y']
    zvals = sample[id][dtype]['z']

    fig = go.Figure()
    fig.add_trace(go.Scatter(x=x, y=xvals, mode='lines+markers', name='x', line_shape='spline'))
    fig.add_trace(go.Scatter(x=x, y=yvals, mode='lines+markers', name='y', line_shape='spline'))
    fig.add_trace(go.Scatter(x=x, y=zvals, mode='lines+markers', name='z', line_shape='spline'))

    fig.update_layout(
        title={
            'text': id,
            'y':0.9,
            'x':0.5,
            'xanchor': 'center',
            'yanchor': 'top'})
    fig.show()


def animate_plot():
    pass
    # try plotly express animations with dataframes


def to_csvs(data_object):
    for id in data_object.keys():
        for dtype in data_object[id].keys():
            with open(f"{id}_{dtype}.csv", 'a+') as f:

                xvals = data_object[id][dtype]['x']
                yvals = data_object[id][dtype]['y']
                zvals = data_object[id][dtype]['z']


                for index in range(len(xvals)):
                    f.writelines(f"{id}, {xvals[index]}, {yvals[index]}, {zvals[index]}\n")



if __name__ == '__main__':
    sample = get_data_object()
    to_csvs(sample)
    # print(sample['0d7c6d6867494c13'].keys())
    # make_plot(sample, id='0d7c6d6867494c13', dtype='Gyroscope')
    # animate_plot()
