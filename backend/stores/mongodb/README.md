MongoDB store
=============

Set environment variables:

    STORE_IMPL=mongo
    MONGO_HOST=localhost
    MONGO_PORT=27017
    MONGO_USERNAME=someuser
    MONGO_PASSWORD=secretpass

Testing with a local MongoDB:

    docker run -it --rm -p 27017:27017 --name mongo mongo:3.6

Local MongoDB web interface:

    docker run --link mongo:mongo -p 8081:8081 mongo-express
