HOST="HOST"
PORT="PORT"
curl -XDELETE "http://${HOST}:${PORT}/ranking"
curl -XPUT "http://${HOST}:${PORT}/ranking" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_doc": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "whitespace"
        },
        "price": {
          "type": "double"
        },
        "ratesum": {
          "type": "integer"
        },
        "category": {
          "type": "keyword"
        }
      }
    }
  }
}'