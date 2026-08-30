#!/usr/bin/env python3
"""生成「天禽」App 自适应图标 VectorDrawable（ic_launcher_foreground.xml）
设计：深色底 + 金色圆环 + 几何天禽（圆头/双三角翼/身/尾/朱砂眼）+ 后天八卦爻环
viewport 108×108，安全区中心 66×66
"""
import math

VP = 108
CX = CY = 54

def circle_path(cx, cy, r):
    # 标准两半圆画法（可靠，无偏移）：上→下→上
    return (f"M{cx:.2f},{cy - r:.2f} "
            f"A{r:.2f},{r:.2f} 0 1,1 {cx:.2f},{cy + r:.2f} "
            f"A{r:.2f},{r:.2f} 0 1,1 {cx:.2f},{cy - r:.2f} Z")

def poly_path(pts):
    d = f"M{pts[0][0]:.2f},{pts[0][1]:.2f}"
    for x, y in pts[1:]:
        d += f" L{x:.2f},{y:.2f}"
    return d + " Z"

def line_path(x1, y1, x2, y2):
    return f"M{x1:.2f},{y1:.2f} L{x2:.2f},{y2:.2f}"

BG = "#14121A"
GOLD = "#D4AF6E"
RED = "#E0301E"

paths = []

# 1. 背景
paths.append((f"M0,0 H{VP} V{VP} H0 Z", BG, None, 0))

# 2. 中心金环（天禽光环）
paths.append((circle_path(CX, CY, 21.0), None, GOLD, 2.6))

# 3. 几何天禽
head_r, head_cy = 4.2, CY - 9.0
paths.append((circle_path(CX, head_cy, head_r), GOLD, None, 0))
paths.append((circle_path(CX + 2.2, head_cy + 0.6, 2.0), RED, None, 0))  # 朱砂眼（放大）
paths.append((poly_path([(CX-17, CY+3), (CX-2, CY+3), (CX-8.5, CY-12)]), GOLD, None, 0))  # 左翼
paths.append((poly_path([(CX+17, CY+3), (CX+2, CY+3), (CX+8.5, CY-12)]), GOLD, None, 0))  # 右翼
paths.append((poly_path([(CX-3, CY+5), (CX+3, CY+5), (CX, CY+13)]), GOLD, None, 0))       # 身
paths.append((poly_path([(CX, CY+13), (CX-2.5, CY+18), (CX+2.5, CY+18)]), GOLD, None, 0)) # 尾

# 4. 后天八卦爻环（0°=北坎，顺时针：坎艮震巽离坤兑乾）
#    三爻（内→外 = 初爻→上爻，标准爻序）：
#    坎☵初阴中阳上阴[0,1,0] 艮☶初阴中阴上阳[0,0,1] 震☳初阳中阴上阴[1,0,0] 巽☴初阴中阳上阳[0,1,1]
#    离☲初阳中阴上阳[1,0,1] 坤☷[0,0,0] 兑☱初阳中阳上阴[1,1,0] 乾☰[1,1,1]
BAGUA = [[0,1,0],[0,0,1],[1,0,0],[0,1,1],[1,0,1],[0,0,0],[1,1,0],[1,1,1]]
YAO_LEN, YAO_GAP, YAO_W, R_IN = 3.2, 1.2, 2.0, 25.2
for i, yao in enumerate(BAGUA):
    ang = math.radians(i * 45 - 90)
    dx, dy = math.cos(ang), math.sin(ang)
    px, py = -dy, dx
    for k, yang in enumerate(yao):
        r = R_IN + k * (YAO_LEN + YAO_GAP) + YAO_LEN / 2
        cx, cy = CX + dx * r, CY + dy * r
        if yang:
            x1 = cx + px * YAO_LEN / 2
            y1 = cy + py * YAO_LEN / 2
            x2 = cx - px * YAO_LEN / 2
            y2 = cy - py * YAO_LEN / 2
            paths.append((line_path(x1, y1, x2, y2), None, GOLD, YAO_W))
        else:
            half, gap = YAO_LEN / 2, 0.5
            for s in (-1, 1):
                paths.append((line_path(cx + px*s*gap, cy + py*s*gap,
                                        cx + px*s*half, cy + py*s*half), None, GOLD, YAO_W))

xml = ['<?xml version="1.0" encoding="utf-8"?>',
       '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
       '    android:width="108dp"',
       '    android:height="108dp"',
       '    android:viewportWidth="108"',
       '    android:viewportHeight="108">']
for d, fill, stroke, sw in paths:
    if fill:
        xml.append(f'    <path android:pathData="{d}" android:fillColor="{fill}"/>')
    else:
        xml.append(f'    <path android:pathData="{d}" android:strokeColor="{stroke}" android:strokeWidth="{sw}" '
                   'android:strokeLineCap="round" android:fillColor="#00000000"/>')
xml.append('</vector>')

out = "/home/potuo/.agent_work/feipan-qimen-app2/app/src/main/res/drawable/ic_launcher_foreground.xml"
with open(out, "w", encoding="utf-8") as f:
    f.write("\n".join(xml))
print(f"written {out}, {len(paths)} paths")
