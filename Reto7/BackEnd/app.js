var createError = require('http-errors');
const express = require('express');
//const admin = require("firebase-admin");
//const serviceAccount = require("./serviceAccountKey.json");
const cors = require('cors');

//admin.initializeApp({credential: admin.credential.cert(serviceAccount)});


var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var indexRouter = require('./routes/index');

var app = express();
const basePath = process.env.PREFIX_PATH || '/';

app.use(logger('dev'));

app.use(express.json({limit: '50mb'}));
app.use(express.urlencoded({limit: '50mb', extended: false}));

app.use(cookieParser());
app.use(cors());

app.use(basePath, indexRouter);

app.use(function (req, res, next) {
    next(createError(404));
});

app.use(function (err, req, res, next) { // set locals, only providing error in development
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {};

    // send the error page
    res.status(err.status || 500);
    res.send('error');
});

module.exports = app;
