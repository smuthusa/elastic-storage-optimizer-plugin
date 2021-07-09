# elastic-storage-optimizer-plugin

This plugin will optimize storage space by doing a mapping for heavy objects to numeric type

## Install Plugin

```
bin/elasticsearch-plugin install file:////<Absolute Path>/elastic-storage-optimizer-plugin/target/releases/storage-optimizer-plugin-1.0-SNAPSHOT.zip
```
## Enable Optimization for index

```
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/_storage/_optimize" -d '{"index": "book", "fields": ["tag"]}' -v
```

## Add Record
```
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/book/index" -d "{\"timestamp\":1624885304,\"name\":\"Yoga for children\",\"Author\":\"Some author\",\"description\":\"for kids to learn yoga\",\"tag\":\"india\"}"
```

## Other examples
```
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/twitter/_doc?routing=saravanan" -d "{\"user\":\"saravanan\",\"post_date\":\"2021-01-15T14:12:12\",\"message\":\"trying out Elasticsearch\"}"
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/twitter/_search?routing=saravanan" -d "{\"query\":{\"bool\":{\"must\":{\"query_string\":{\"query\":\"trying\"}},\"filter\":{\"term\":{\"user\":\"saravanan\"}}}}}"
```