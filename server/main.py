import time
from flask import Flask, request
from flask import jsonify
import os

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = '/static/uploads/'
contentList = []


def inputFile(file_name):
    f = open(file_name, 'r', encoding='utf-8')
    line = f.readline()
    contentList.clear()
    while line:
        contentList.append(line)
        line = f.readline()
    f.close()


def prepare(file_name):
    start_time = time.clock()
    inputFile(file_name)
    diff_time = time.clock() - start_time
    print("加载文件耗时:" + str(diff_time))


def searchKeyWord(key, upload_time):
    start_time = time.clock()
    count = 0
    for s in contentList:
        count += s.count(key)
    return response(200,
                    {"keyword": key, "upload_time": upload_time, "count": count, "time": time.clock() - start_time})


def response(code, msg):
    return {"code": code, "data": msg}


@app.route('/remote', methods=['POST'])
def upload():
    upload_start_time = time.clock()
    upload_file = request.files['file']
    file_name = upload_file.filename
    upload_file.save(os.path.join(app.root_path, app.config['UPLOAD_FOLDER'], file_name))
    upload_time = time.clock() - upload_start_time
    prepare("c://static/uploads/" + file_name)
    return jsonify(searchKeyWord("网络", upload_time))


if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=80)
