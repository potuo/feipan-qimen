#!/usr/bin/env python3
"""天禽 App logo 候选 mockup 绘制（PIL）"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math

S = 1024  # 画布尺寸
FONT = "/usr/share/fonts/TTF/LXGWWenKaiGBScreen.ttf"

def base_canvas():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)

def rounded_bg(draw, color, radius=200, margin=0):
    draw.rounded_rectangle([margin, margin, S-margin, S-margin], radius=radius, fill=color)

def save(img, path):
    img.save(path)
    print(f"saved {path}")

GOLD = (212, 175, 110, 255)
GOLD_D = (180, 140, 80, 255)
INK = (24, 22, 26, 255)
CINNABAR = (192, 57, 43, 255)
PAPER = (242, 237, 227, 255)

# ═══════════════════════════════════════════
# 方案 A：九宫格天禽（主推）
# 深墨圆角底 + 金色九宫格 + 中宫金环天禽星芒
# ═══════════════════════════════════════════
img, d = base_canvas()
rounded_bg(d, INK, radius=230)
grid_margin = 130
cell = (S - 2*grid_margin) / 3
lw = 14
# 九宫格线（金色）
for i in range(4):
    p = grid_margin + i * cell
    d.line([grid_margin, p, S-grid_margin, p], fill=GOLD, width=lw)
    d.line([p, grid_margin, p, S-grid_margin], fill=GOLD, width=lw)
# 中宫金环（天禽）
cx, cy = S/2, S/2
r_outer = cell * 0.62
d.ellipse([cx-r_outer, cy-r_outer, cx+r_outer, cy+r_outer], outline=GOLD, width=16)
# 内环
d.ellipse([cx-r_outer*0.62, cy-r_outer*0.62, cx+r_outer*0.62, cy+r_outer*0.62], outline=GOLD, width=8)
# 中央禽羽星芒：三片羽毛（弧线组合）+ 中心点
for k in range(3):
    ang = k * 120 - 90
    for t in range(10):
        a1 = math.radians(ang - 6)
        a2 = math.radians(ang + 6)
        r1 = r_outer * 0.30 + t * (r_outer * 0.18 / 10)
        r2 = r1 + r_outer * 0.18 / 10
        d.arc([cx-r2, cy-r2, cx+r2, cy+r2], math.degrees(a1), math.degrees(a2), fill=GOLD, width=7)
d.ellipse([cx-26, cy-26, cx+26, cy+26], fill=CINNABAR)  # 朱砂中心
save(img, "/home/potuo/.agent_work/feipan-qimen-app2/logo_mockup_A_jiugong.png")

# ═══════════════════════════════════════════
# 方案 B：天禽罗盘（金环罗盘 + 中宫禽鸟剪影）
# ═══════════════════════════════════════════
img, d = base_canvas()
rounded_bg(d, INK, radius=230)
cx, cy = S/2, S/2
R = 330
# 外罗盘环
d.ellipse([cx-R, cy-R, cx+R, cy+R], outline=GOLD, width=16)
d.ellipse([cx-R*0.82, cy-R*0.82, cx+R*0.82, cy+R*0.82], outline=GOLD, width=6)
# 罗盘刻度（八卦方位 8 刻度长线 + 24 短线）
for i in range(24):
    a = math.radians(i * 15 - 90)
    outer = R * 0.82
    if i % 3 == 0:
        inner = R * 0.62
        w = 10
    else:
        inner = R * 0.70
        w = 5
    d.line([cx + inner*math.cos(a), cy + inner*math.sin(a),
            cx + outer*math.cos(a), cy + outer*math.sin(a)], fill=GOLD, width=w)
# 中宫禽鸟剪影（几何飞鸟：双翼 + 身 + 尾）
bird = [(cx-90, cy+10), (cx-30, cy-55), (cx+10, cy-25), (cx+80, cy-40),
        (cx+45, cy-5), (cx+95, cy+30), (cx+15, cy+18), (cx+8, cy+55), (cx-25, cy+28)]
d.polygon(bird, fill=GOLD)
# 眼点（朱砂）
d.ellipse([cx+28, cy-30, cx+44, cy-14], fill=CINNABAR)
save(img, "/home/potuo/.agent_work/feipan-qimen-app2/logo_mockup_B_luopan.png")

# ═══════════════════════════════════════════
# 方案 C：天禽印章（朱砂印 + 文楷「禽」字 + 九宫暗纹）
# ═══════════════════════════════════════════
img, d = base_canvas()
rounded_bg(d, CINNABAR, radius=200)
# 印章边框（双线）
b1, b2 = 70, 100
d.rounded_rectangle([b1, b1, S-b1, S-b1], radius=150, outline=(255, 240, 235, 255), width=8)
d.rounded_rectangle([b2, b2, S-b2, S-b2], radius=135, outline=(255, 240, 235, 255), width=4)
# 中央「禽」字（文楷，白色）
font = ImageFont.truetype(FONT, 460)
txt = "禽"
bbox = d.textbbox((0, 0), txt, font=font)
tw, th = bbox[2]-bbox[0], bbox[3]-bbox[1]
d.text((cx - tw/2 - bbox[0], cy - th/2 - bbox[1]), txt, font=font, fill=(255, 244, 238, 255))
# 底部小字「飞盘奇门」（小印章）
font_s = ImageFont.truetype(FONT, 90)
txt2 = "飞盘奇门"
bbox2 = d.textbbox((0, 0), txt2, font=font_s)
tw2 = bbox2[2]-bbox2[0]
d.text((cx - tw2/2 - bbox2[0], S - 190), txt2, font=font_s, fill=(255, 236, 230, 230))
save(img, "/home/potuo/.agent_work/feipan-qimen-app2/logo_mockup_C_yinzhang.png")

print("done")
