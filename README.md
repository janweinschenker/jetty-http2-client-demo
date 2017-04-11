# Example Code for the Jetty HTTP/2 client

## Usage

This example needs JDK 1.8.0_60 or newer.

1. Checkout project at [https://github.com/janweinschenker/simple-rest-service](https://github.com/janweinschenker/simple-rest-service)
1. Run the rest service as it is described in its readme.
1. Checkout this Jetty example
1. run once: 
   1. `$ cd script`
   1. `$ ./import-server-cert.sh` to import the self signed certificate into your jvm's keystore.
   1. `$ cd ..`
1. run `$ mvn clean package exec:exec`

