HOST="HOST"
PORT="PORT"
curl -XDELETE "http://${HOST}:${PORT}/excellent_items"
curl -XPUT "http://${HOST}:${PORT}/excellent_items" -H 'Content-Type: application/json' -d'
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
        "related_items": {
          "type": "keyword"
        }
      }
    }
  }
}'
curl -XDELETE "http://${HOST}:${PORT}/good_items"
curl -XPUT "http://${HOST}:${PORT}/good_items" -H 'Content-Type: application/json' -d'
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
        "related_items": {
          "type": "keyword"
        }
      }
    }
  }
}'
curl -XDELETE "http://${HOST}:${PORT}/bad_items"
curl -XPUT "http://${HOST}:${PORT}/bad_items" -H 'Content-Type: application/json' -d'
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
        "related_items": {
          "type": "keyword"
        }
      }
    }
  }
}'