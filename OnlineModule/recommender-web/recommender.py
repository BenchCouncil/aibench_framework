import time
import fasttext
import requests
from flask import Flask
from flask import Response
from flask import request
from neo4j import GraphDatabase

app = Flask(__name__)
category_classifier = fasttext.load_model('category_classifier.ftz')
driver = GraphDatabase.driver("bolt://localhost:7687", auth=('neo4j', 'neo4j'))


@app.route("/")
def hello():
    return "Hello World from Flask in a uWSGI Nginx Docker container with \
     Python 3.7 (from the example template)"


@app.route("/query", methods=['POST'])
def analyse_query():
    timestamps = list()
    timestamps.append(int(round(time.time() * 1000)))

    data = request.get_json()
    qid = int(data['qid'])
    uid = data['uid']
    query = data['query']

    timestamps.append(int(round(time.time() * 1000)))
    with driver.session() as session:
        result = session.run("MATCH (user:User {uid: $uid}) RETURN user", uid=uid)
    record = result.single()
    user = record['user']

    timestamps.append(int(round(time.time() * 1000)))
    category = int(category_classifier.predict(query)[0][0][9:])

    timestamps.append(int(round(time.time() * 1000)))
    payload = {
        "instances": [
            {
                "category": [category],
                "sex": [user['sex']],
                "age": [user['age']],
                "power": [user['power']]
            }
        ]
    }
    session = requests.session()
    session.keep_alive = False
    ranking_weights = session.post('http://localhost:8501/v1/models/ranking_weights_model:predict', json=payload)

    timestamps.append(int(round(time.time() * 1000)))
    logging.info(str(qid) + ":" + ",".join(str(timestamp) for timestamp in timestamps))
    return Response(ranking_weights.content, mimetype="application/json")


if __name__ == "__main__":
    import logging

    logging.basicConfig(filename='recommender.log', level=logging.INFO)

    # Only for debugging while developing
    app.run(host='0.0.0.0', port=8080)
