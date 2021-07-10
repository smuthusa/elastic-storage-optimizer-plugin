# elastic-storage-optimizer-plugin

This plugin will optimize storage space by doing a mapping for heavy objects to numeric type

## Dependency
```
Java 11
Maven 3.6.x
```
## Compile
```
mvn clean install
```

## Install Plugin

```
bin/elasticsearch-plugin install file:////<Absolute Path>/elastic-storage-optimizer-plugin/target/releases/storage-optimizer-plugin-1.0-SNAPSHOT.zip
```
## Enable Optimization for index

```
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/_storage/_optimize" -d '{"index": "stats", "fields": ["region"]}' -v
```

## Examples
```
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/stats/_doc" -d "{\"hostname\":\"madelephant\",\"post_date\":\"2021-01-15T14:12:12\",\"region\":\"London\"}"
curl -H "Content-Type: application/json" -XPOST "http://localhost:9200/stats/_search" -d "{\"query\":{\"bool\":{\"must\":\"filter\":{\"term\":{\"region\":\"London\"}}}}}"
```