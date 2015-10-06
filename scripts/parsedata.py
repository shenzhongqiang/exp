import datetime

def parse_file(filepath):
    f1 = open(filepath)
    content = f1.read()
    f1.close()

    lines = content.split("\n")
    data = []
    for line in lines:
        parts = line.split(",")
        if len(parts) < 3:
            continue
        parts = map(lambda x: x.replace('"', ''), parts)
        time = datetime.datetime.strptime(parts[0], "%Y-%m-%d %H:%M:%S")
        data.append({"time": time, "close": parts[2]})
    return data

gold = parse_file("src/main/java/history/XAUUSDH1")
silver = parse_file("src/main/java/history/XAGUSDH1")

gold.reverse()
silver.reverse()

results = []
i = 0
j = 0
while(i < len(gold) and j < len(silver)):
    if gold[i]["time"] < silver[j]["time"]:
        i+=1
    elif gold[i]["time"] == silver[j]["time"]:
        results.append({"gold":gold[i],"silver":silver[j]})
        i+=1
        j+=1
    else:
        j+=1


k = 1
while(k < len(results)):
    gprice = float(results[k]["gold"]["close"])
    gchg = float(results[k]["gold"]["close"]) / float(results[k-1]["gold"]["close"]) - 1
    sprice = float(results[k]["silver"]["close"])
    schg = float(results[k]["silver"]["close"]) / float(results[k-1]["silver"]["close"]) - 1
    ratio = gprice / sprice
    diff = gchg - schg
    print "%s,%s,%s,%f,%f,%f,%f" % (
        results[k]["gold"]["time"],
        results[k]["gold"]["close"],
        results[k]["silver"]["close"],
        abs(gchg),
        abs(schg),
        diff,
        ratio)
    k+=1
