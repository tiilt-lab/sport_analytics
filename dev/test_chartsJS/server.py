import json
import random
import time
from datetime import datetime

from flask import Flask, Response, render_template

application = Flask(__name__)
random.seed()  # Initialize the random number generator

user = ""
dtype = ""


@application.route('/')
def index():
    return '<p>Hello, World</p>'


@application.route('/chart/<id>')
def chart(id):
    global user
    user = id
    return render_template('./index.html', value=id)


@application.route('/chart-data')
def chart_data():
    global user, dtype
    def generate_random_data():
        while True:
            json_data = json.dumps(
                {'time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                 'value': random.random() * 100})
            yield f"data:{json_data}\n\n"
            time.sleep(1)

    def send_csv_data():
        print(f"***{user}***")
        if user == "":
            return
        with open(f"../session_parsing/{user}_Accelerometer.csv", 'r+') as f:
            for line in f:
                line_info = line.split(',')
                json_data = json.dumps(
                    {'time': line_info[0],
                     # 'time': str(datetime.fromtimestamp(int(line_info[0]))),
                     'value0': [line_info[1]],
                     'value1': line_info[2],
                     'value2': line_info[3], })
                yield f"data:{json_data}\n\n"
                time.sleep(0.1)

    return Response(send_csv_data(), mimetype='text/event-stream')


if __name__ == '__main__':
    application.run(debug=True, threaded=True)

"""
TODO
navigation to files
    popup menu?
    prompt?
    send clustered data

    just focus on acc data
"""