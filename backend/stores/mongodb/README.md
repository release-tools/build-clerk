MongoDB store
=============

Set environment variables:

    REPORT_STORE_IMPL="com.gatehill.buildclerk.dao.mongo.MongoBuildReportDaoImpl"
    MONGO_HOST="localhost"
    MONGO_PORT="27017"

Testing with a local MongoDB:

    docker run -it --rm -p 27017:27017 --name mongo mongo:3.6

Local MongoDB web interface:

    docker run --link mongo:mongo -p 8081:8081 mongo-express