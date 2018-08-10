# MQTT-client-example

## Build

```
$ ./gradlew installDist
```

## Usage

```
$ build/install/mqtt_client_example/bin/mqtt_client_example --endpoint ssl://foobar.iot.us-east-1.amazonaws.com:8883 --cert-file /tmp/mqtt_client_test/foo-bar-thing.cert.pem --key-file /tmp/mqtt_client_test/foo-bar-thing.private.key --topic sdk/test/java --message "Hello world" --client-id MqttClientTest
```
