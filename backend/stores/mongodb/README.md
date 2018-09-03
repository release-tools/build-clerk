MongoDB store
=============

Set environment variables:

    STORE_IMPL=mongo
    MONGO_HOST=localhost
    MONGO_PORT=27017
    MONGO_USERNAME=someuser
    MONGO_PASSWORD=secretpass

## Testing

### On Docker

Testing with a local MongoDB:

    docker run -it --rm -p 27017:27017 --name mongo mongo:3.6

Local MongoDB web interface:

    docker run --link mongo:mongo -p 8081:8081 mongo-express

### On Kubernetes (using Helm)

You can install a MongoDB Helm chart as follows:

    helm upgrade --install mongo stable/mongodb \
        --set mongodbUsername=clerk,mongodbPassword=clerk,mongodbDatabase=clerk

> Note: Make sure to set the username and password to match Clerk's environment variables.
