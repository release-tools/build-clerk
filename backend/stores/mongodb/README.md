MongoDB store
=============

Set environment variable:

    REPORT_STORE_IMPL="com.gatehill.buildclerk.dao.mongo.MongoBuildReportDaoImpl"

Testing with a local MongoDB:

    docker run -it --rm -p27017:27017 mongo:3.6
