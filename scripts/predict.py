import sys
import numpy as np
import matplotlib.pyplot as plt
import sklearn
from sklearn.linear_model import LinearRegression
from sklearn.svm import SVR
from parsedata import *

def get_series_within_range(rangefrom, rangeto):
    try:
        from_dt = datetime.datetime.strptime(rangefrom, "%Y-%m-%d %H:%M:%S")
        to_dt = datetime.datetime.strptime(rangeto, "%Y-%m-%d %H:%M:%S")

        result = []
        for bar in series:
            bar_time = bar["time"]
            bar_close = bar["close"]
            if bar_time >= from_dt and bar_time < to_dt:
                result.append(bar)
        return result
    except ValueError, e:
        print "Unable to parse datetime. Datetime needs to be in format %Y-%m-%d %H:%M:%S."
        return []

def get_low(timeseries, n):
    result = []
    if len(timeseries) < n:
        raise Exception("timeseries too short")

    i = 0
    while i < len(timeseries):
        if i < n-1:
            result.append({"time": timeseries[i]["time"], "value": 0.0})
        else:
            closes = map(lambda x: x["low"], timeseries[i-n+1:i+1])
            low = min(closes)
            result.append({"time": timeseries[i]["time"], "value": low})
        i+=1
    return result

def get_ema(timeseries, n):
    result = []
    if len(timeseries) < n*2:
        raise Exception("timeseries too short")

    i = 0
    while i < len(timeseries):
        if i < n-1:
            result.append({"time": timeseries[i]["time"], "value": 0.0})
        if i == n-1:
            closes = map(lambda x: x["close"], timeseries[0:n])
            sma = sum(closes) / n
            result.append({"time": timeseries[i]["time"], "value": sma})
        if i > n-1:
            ema = 2.0/(n+1) * (timeseries[i]["close"]-result[i-1]["value"]) + result[i-1]["value"]
            result.append({"time": timeseries[i]["time"], "value": ema})
        i+=1
    return result

def get_first_order_diff(timeseries):
    result = []
    i = 0
    while i < len(timeseries):
        if i == 0:
            result.append({"time": timeseries[i]["time"], "value": 0})
        else:
            diff = timeseries[i]["value"] - timeseries[i-1]["value"]
            result.append({"time": timeseries[i]["time"], "value": diff})
        i+=1
    return result

def get_second_order_diff(timeseries):
    result = []
    i = 0
    while i < len(timeseries):
        if i < 2:
            result.append({"time": timeseries[i]["time"], "value": 0})
        else:
            diff = timeseries[i]["value"] - timeseries[i-1]["value"]
            result.append({"time": timeseries[i]["time"], "value": diff})
        i+=1
    return result

series = parse_file("src/main/java/history/EURUSDm5")

rangefrom = "2015-09-01 05:00:00"
rangeto = "2015-10-23 05:00:00"
timeseries = get_series_within_range(rangefrom, rangeto)
closes = map(lambda x: {"time": x["time"], "close": x["close"]}, timeseries)
lowseries = map(lambda x: {"time": x["time"], "value": x["low"]}, timeseries)
#for data in lowseries:
#    value = data["value"]
#    dt = data["time"]
#    str_dt = dt.strftime("%Y-%m-%d %H:%M:%S")
#    print str_dt, value
first_order_diff = get_first_order_diff(lowseries)

lm = LinearRegression()
svr = SVR(kernel='rbf', C=1e3)
seq = range(50)
X = np.array([seq]).T
k = 50
print "time\tcoef\tmse\tavg\tdiff"
while k < len(lowseries):
    time = lowseries[k]["time"]
    data = map(lambda x: x["value"], lowseries[k-50:k])
    Y = np.array(data) * 1e4
    avg = np.mean(Y)
    svr.fit(X,Y)
    mse = np.mean((Y - svr.predict(X)) ** 2)
    if mse < 0.009:
        print "%s\t%0.3f" % (time, mse)
    k+=1

