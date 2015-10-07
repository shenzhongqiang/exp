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
        data.append({"time": time,
            "open": float(parts[1]),
            "close": float(parts[2]),
            "high": float(parts[3]),
            "low": float(parts[4])})
    data.reverse()
    return data

if __name__ == "__main__":
    gold = parse_file("src/main/java/history/XAUUSDH1")
    silver = parse_file("src/main/java/history/XAGUSDH1")

