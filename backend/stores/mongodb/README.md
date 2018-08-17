MongoDB store
=============

Set environment variables:

    REPORT_STORE_IMPL="com.gatehill.buildclerk.dao.mongo.MongoBuildReportDaoImpl"
    MONGO_HOST="localhost"
    MONGO_PORT="27017"

Testing with a local MongoDB:

    docker run -it --rm -p27017:27017 mongo:3.6
