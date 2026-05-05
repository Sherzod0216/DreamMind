from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from html import escape
from pathlib import Path
import struct
from zipfile import ZIP_DEFLATED, ZipFile


EMU_PER_INCH = 914400
SLIDE_W = 12192000
SLIDE_H = 6858000

NS_A = "http://schemas.openxmlformats.org/drawingml/2006/main"
NS_P = "http://schemas.openxmlformats.org/presentationml/2006/main"
NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

BG = "0B0F1F"
SURFACE = "10162C"
CARD = "161B33"
CARD_ALT = "1F2547"
PRIMARY = "7C6FF0"
PRIMARY_SOFT = "A69DFF"
ACCENT = "7FE3C4"
TEXT = "F4F5FB"
MUTED = "9AA0C0"
BORDER = "262C48"
DANGER = "FF6B6B"
PINK = "E98DB3"


def emu(value_in_inches: float) -> int:
    return int(value_in_inches * EMU_PER_INCH)


def alpha_xml(value: int | None) -> str:
    if value is None:
        return ""
    return f"<a:alpha val=\"{value}\"/>"


def solid_fill(color: str, alpha: int | None = None) -> str:
    return f"<a:solidFill><a:srgbClr val=\"{color}\">{alpha_xml(alpha)}</a:srgbClr></a:solidFill>"


def line_xml(color: str | None = None, width: int = 12700, alpha: int | None = None) -> str:
    if color is None:
        return "<a:ln><a:noFill/></a:ln>"
    return (
        f"<a:ln w=\"{width}\">"
        f"{solid_fill(color, alpha)}"
        "<a:prstDash val=\"solid\"/>"
        "</a:ln>"
    )


def body_pr(anchor: str = "ctr") -> str:
    return f"<a:bodyPr wrap=\"square\" rtlCol=\"0\" anchor=\"{anchor}\"/><a:lstStyle/>"


def run_xml(
    text: str,
    size_pt: float,
    color: str,
    font: str,
    bold: bool = False,
) -> str:
    size = int(size_pt * 100)
    bold_attr = " b=\"1\"" if bold else ""
    return (
        f"<a:r><a:rPr lang=\"en-US\" sz=\"{size}\"{bold_attr}>"
        f"{solid_fill(color)}"
        f"<a:latin typeface=\"{font}\"/>"
        f"</a:rPr><a:t>{escape(text)}</a:t></a:r>"
    )


def paragraph_xml(
    text: str,
    size_pt: float,
    color: str,
    font: str = "Aptos",
    bold: bool = False,
    align: str = "l",
) -> str:
    return (
        f"<a:p><a:pPr algn=\"{align}\"/>"
        f"{run_xml(text, size_pt, color, font, bold)}"
        f"<a:endParaRPr lang=\"en-US\" sz=\"{int(size_pt * 100)}\"/></a:p>"
    )


def multi_paragraph_xml(
    text: str,
    size_pt: float,
    color: str,
    font: str = "Aptos",
    bold: bool = False,
    align: str = "l",
) -> str:
    parts = text.split("\n")
    return "".join(paragraph_xml(part, size_pt, color, font, bold, align) for part in parts)


@dataclass
class Slide:
    name: str
    shapes: list[str] = field(default_factory=list)
    image_rels: list[tuple[str, Path]] = field(default_factory=list)
    next_shape_id: int = 2
    next_rel_num: int = 2

    def add_xml(self, xml: str) -> None:
        self.shapes.append(xml)

    def alloc_id(self) -> int:
        current = self.next_shape_id
        self.next_shape_id += 1
        return current

    def alloc_rel_id(self) -> str:
        rel_id = f"rId{self.next_rel_num}"
        self.next_rel_num += 1
        return rel_id

    def rect(
        self,
        x: int,
        y: int,
        cx: int,
        cy: int,
        *,
        fill: str | None = None,
        fill_alpha: int | None = None,
        line: str | None = None,
        line_alpha: int | None = None,
        round_rect: bool = False,
        text: str | None = None,
        text_color: str = TEXT,
        text_size: float = 18,
        bold: bool = False,
        font: str = "Aptos",
        align: str = "l",
        valign: str = "ctr",
        margins: tuple[int, int, int, int] | None = None,
        name: str = "Shape",
    ) -> None:
        shape_id = self.alloc_id()
        geom = "roundRect" if round_rect else "rect"
        if margins is None:
            mar_l = mar_r = emu(0.08)
            mar_t = mar_b = emu(0.03)
        else:
            mar_l, mar_r, mar_t, mar_b = margins
        fill_xml = solid_fill(fill, fill_alpha) if fill else "<a:noFill/>"
        line_markup = line_xml(line, alpha=line_alpha) if line or line is None else "<a:ln><a:noFill/></a:ln>"
        if line is None and fill is not None:
            line_markup = "<a:ln><a:noFill/></a:ln>"
        tx_body = ""
        if text is not None:
            tx_body = (
                "<p:txBody>"
                f"<a:bodyPr wrap=\"square\" rtlCol=\"0\" anchor=\"{valign}\" "
                f"lIns=\"{mar_l}\" rIns=\"{mar_r}\" tIns=\"{mar_t}\" bIns=\"{mar_b}\"/>"
                "<a:lstStyle/>"
                f"{multi_paragraph_xml(text, text_size, text_color, font, bold, align)}"
                "</p:txBody>"
            )
        self.add_xml(
            f"<p:sp>"
            f"<p:nvSpPr><p:cNvPr id=\"{shape_id}\" name=\"{escape(name)} {shape_id}\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>"
            f"<p:spPr><a:xfrm><a:off x=\"{x}\" y=\"{y}\"/><a:ext cx=\"{cx}\" cy=\"{cy}\"/></a:xfrm>"
            f"<a:prstGeom prst=\"{geom}\"><a:avLst/></a:prstGeom>"
            f"{fill_xml}{line_markup}</p:spPr>"
            f"{tx_body}</p:sp>"
        )

    def ellipse(
        self,
        x: int,
        y: int,
        cx: int,
        cy: int,
        *,
        fill: str,
        fill_alpha: int | None = None,
        line: str | None = None,
        line_alpha: int | None = None,
        text: str | None = None,
        text_color: str = TEXT,
        text_size: float = 18,
        bold: bool = False,
        font: str = "Aptos",
        align: str = "ctr",
        name: str = "Ellipse",
    ) -> None:
        shape_id = self.alloc_id()
        tx_body = ""
        if text is not None:
            tx_body = (
                "<p:txBody>"
                f"{body_pr('ctr')}"
                f"{multi_paragraph_xml(text, text_size, text_color, font, bold, align)}"
                "</p:txBody>"
            )
        self.add_xml(
            f"<p:sp>"
            f"<p:nvSpPr><p:cNvPr id=\"{shape_id}\" name=\"{escape(name)} {shape_id}\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>"
            f"<p:spPr><a:xfrm><a:off x=\"{x}\" y=\"{y}\"/><a:ext cx=\"{cx}\" cy=\"{cy}\"/></a:xfrm>"
            "<a:prstGeom prst=\"ellipse\"><a:avLst/></a:prstGeom>"
            f"{solid_fill(fill, fill_alpha)}{line_xml(line, alpha=line_alpha)}</p:spPr>"
            f"{tx_body}</p:sp>"
        )

    def text(
        self,
        x: int,
        y: int,
        cx: int,
        cy: int,
        text: str,
        *,
        color: str = TEXT,
        size: float = 20,
        bold: bool = False,
        font: str = "Aptos",
        align: str = "l",
        valign: str = "t",
        name: str = "TextBox",
    ) -> None:
        self.rect(
            x,
            y,
            cx,
            cy,
            fill=None,
            line="",
            text=text,
            text_color=color,
            text_size=size,
            bold=bold,
            font=font,
            align=align,
            valign=valign,
            margins=(0, 0, 0, 0),
            name=name,
        )

    def picture(
        self,
        path: Path,
        x: int,
        y: int,
        cx: int,
        cy: int,
        *,
        name: str = "Picture",
        line: str | None = None,
        line_alpha: int | None = None,
    ) -> None:
        rel_id = self.alloc_rel_id()
        self.image_rels.append((rel_id, path))
        shape_id = self.alloc_id()
        self.add_xml(
            f"<p:pic>"
            f"<p:nvPicPr><p:cNvPr id=\"{shape_id}\" name=\"{escape(name)} {shape_id}\"/>"
            f"<p:cNvPicPr><a:picLocks noChangeAspect=\"1\"/></p:cNvPicPr><p:nvPr/></p:nvPicPr>"
            f"<p:blipFill><a:blip r:embed=\"{rel_id}\"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>"
            f"<p:spPr><a:xfrm><a:off x=\"{x}\" y=\"{y}\"/><a:ext cx=\"{cx}\" cy=\"{cy}\"/></a:xfrm>"
            f"<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>{line_xml(line, alpha=line_alpha)}</p:spPr>"
            f"</p:pic>"
        )

    def build_xml(self) -> str:
        sp_tree = (
            "<p:spTree>"
            "<p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>"
            "<p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/>"
            "<a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr>"
            + "".join(self.shapes)
            + "</p:spTree>"
        )
        return (
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            f"<p:sld xmlns:a=\"{NS_A}\" xmlns:r=\"{NS_R}\" xmlns:p=\"{NS_P}\">"
            f"<p:cSld name=\"{escape(self.name)}\">{sp_tree}</p:cSld>"
            "<p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>"
            "</p:sld>"
        )


class PhoneCanvas:
    def __init__(self, slide: Slide, x: int, y: int, w: int, h: int) -> None:
        self.slide = slide
        self.x = x
        self.y = y
        self.w = w
        self.h = h
        self.inner_x = x + int(w * 0.06)
        self.inner_y = y + int(h * 0.045)
        self.inner_w = int(w * 0.88)
        self.inner_h = int(h * 0.91)
        self.font_scale = w / emu(2.15)
        self._frame()

    def _frame(self) -> None:
        self.slide.rect(self.x, self.y, self.w, self.h, fill="0E1326", line=BORDER, round_rect=True, name="PhoneFrame")
        self.slide.rect(
            self.inner_x,
            self.inner_y,
            self.inner_w,
            self.inner_h,
            fill=BG,
            line=BORDER,
            line_alpha=35000,
            round_rect=True,
            name="Screen",
        )
        self.slide.rect(
            self.x + int(self.w * 0.34),
            self.y + int(self.h * 0.018),
            int(self.w * 0.32),
            int(self.h * 0.02),
            fill=CARD_ALT,
            round_rect=True,
            name="Speaker",
        )

    def px(self, value: float) -> int:
        return self.inner_x + int(self.inner_w * value / 1000)

    def py(self, value: float) -> int:
        return self.inner_y + int(self.inner_h * value / 2000)

    def pw(self, value: float) -> int:
        return int(self.inner_w * value / 1000)

    def ph(self, value: float) -> int:
        return int(self.inner_h * value / 2000)

    def text_size(self, base: float) -> float:
        return max(7.0, base * self.font_scale)

    def rr(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        *,
        fill: str,
        fill_alpha: int | None = None,
        line: str | None = None,
        line_alpha: int | None = None,
        text: str | None = None,
        text_color: str = TEXT,
        text_size: float = 16,
        bold: bool = False,
        align: str = "l",
        valign: str = "ctr",
        name: str = "PhoneCard",
    ) -> None:
        self.slide.rect(
            self.px(x),
            self.py(y),
            self.pw(w),
            self.ph(h),
            fill=fill,
            fill_alpha=fill_alpha,
            line=line,
            line_alpha=line_alpha,
            round_rect=True,
            text=text,
            text_color=text_color,
            text_size=self.text_size(text_size),
            bold=bold,
            align=align,
            valign=valign,
            margins=(self.pw(18), self.pw(18), self.ph(10), self.ph(10)),
            name=name,
        )

    def rect(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        *,
        fill: str,
        fill_alpha: int | None = None,
        line: str | None = None,
        line_alpha: int | None = None,
        name: str = "PhoneRect",
    ) -> None:
        self.slide.rect(
            self.px(x),
            self.py(y),
            self.pw(w),
            self.ph(h),
            fill=fill,
            fill_alpha=fill_alpha,
            line=line,
            line_alpha=line_alpha,
            round_rect=False,
            name=name,
        )

    def circle(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        *,
        fill: str,
        fill_alpha: int | None = None,
        line: str | None = None,
        line_alpha: int | None = None,
        text: str | None = None,
        text_color: str = TEXT,
        text_size: float = 16,
        bold: bool = False,
        name: str = "PhoneCircle",
    ) -> None:
        self.slide.ellipse(
            self.px(x),
            self.py(y),
            self.pw(w),
            self.ph(h),
            fill=fill,
            fill_alpha=fill_alpha,
            line=line,
            line_alpha=line_alpha,
            text=text,
            text_color=text_color,
            text_size=self.text_size(text_size),
            bold=bold,
            font="Aptos",
            name=name,
        )

    def label(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        text: str,
        *,
        size: float,
        color: str = TEXT,
        bold: bool = False,
        align: str = "l",
        font: str = "Aptos",
    ) -> None:
        self.slide.text(
            self.px(x),
            self.py(y),
            self.pw(w),
            self.ph(h),
            text,
            color=color,
            size=self.text_size(size),
            bold=bold,
            font=font,
            align=align,
            valign="t",
        )

    def dot_bg(self) -> None:
        for x, y, s, c, a in [
            (90, 120, 10, TEXT, 25000),
            (760, 210, 12, PRIMARY, 26000),
            (690, 620, 8, TEXT, 18000),
            (170, 1320, 10, PRIMARY_SOFT, 18000),
            (820, 1520, 10, ACCENT, 24000),
        ]:
            self.circle(x, y, s, s, fill=c, fill_alpha=a, line=None)

    def bottom_nav(self, selected_index: int = 0) -> None:
        self.rr(80, 1820, 840, 120, fill=CARD_ALT, fill_alpha=90000, line=BORDER, line_alpha=35000, name="BottomBar")
        widths = [110, 110, 110, 110, 110]
        start_x = 110
        for idx, width in enumerate(widths):
            fill = PRIMARY if idx == selected_index else BORDER
            alpha = 100000 if idx == selected_index else 45000
            self.rr(start_x + idx * 155, 1860, width, 24, fill=fill, fill_alpha=alpha, line=None, name="TabDot")


def image_size(path: Path) -> tuple[int, int]:
    suffix = path.suffix.lower()
    if suffix == ".png":
        with path.open("rb") as handle:
            header = handle.read(24)
        if header[:8] != b"\x89PNG\r\n\x1a\n":
            raise ValueError(f"Unsupported PNG file: {path}")
        width, height = struct.unpack(">II", header[16:24])
        return width, height
    if suffix in {".jpg", ".jpeg"}:
        data = path.read_bytes()
        offset = 2
        while offset < len(data):
            if data[offset] != 0xFF:
                offset += 1
                continue
            marker = data[offset + 1]
            if marker in (0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF):
                height, width = struct.unpack(">HH", data[offset + 5: offset + 9])
                return width, height
            if marker == 0xDA:
                break
            segment_length = struct.unpack(">H", data[offset + 2: offset + 4])[0]
            offset += 2 + segment_length
        raise ValueError(f"Unsupported JPEG file: {path}")
    raise ValueError(f"Unsupported image format: {path.suffix}")


def fit_box(box_w: int, box_h: int, img_w: int, img_h: int) -> tuple[int, int]:
    scale = min(box_w / img_w, box_h / img_h)
    return int(img_w * scale), int(img_h * scale)


def add_screenshot_card(
    slide: Slide,
    image_path: Path,
    x: float,
    y: float,
    w: float,
    h: float,
    title: str,
    caption: str = "",
    accent: str = PRIMARY,
) -> None:
    slide.rect(
        emu(x),
        emu(y),
        emu(w),
        emu(h),
        fill=CARD,
        line=BORDER,
        line_alpha=30000,
        round_rect=True,
        name="ScreenshotCard",
    )
    slide.rect(emu(x + 0.18), emu(y + 0.18), emu(0.12), emu(h - 0.36), fill=accent, line=None, round_rect=True, name="AccentBar")
    slide.text(emu(x + 0.42), emu(y + 0.18), emu(w - 0.7), emu(0.32), title, color=TEXT, size=12.8, bold=True)
    if caption:
        slide.text(emu(x + 0.42), emu(y + h - 0.42), emu(w - 0.68), emu(0.22), caption, color=MUTED, size=8.8)

    pad_x = emu(0.2)
    pad_top = emu(0.55)
    pad_bottom = emu(0.52 if caption else 0.24)
    pic_box_w = emu(w) - pad_x * 2
    pic_box_h = emu(h) - pad_top - pad_bottom
    img_w, img_h = image_size(image_path)
    pic_w, pic_h = fit_box(pic_box_w, pic_box_h, img_w, img_h)
    pic_x = emu(x) + (emu(w) - pic_w) // 2
    pic_y = emu(y) + pad_top + (pic_box_h - pic_h) // 2
    slide.picture(image_path, pic_x, pic_y, pic_w, pic_h, name=title)


def shots_root() -> Path:
    return Path(__file__).resolve().parents[1]


SHOT_WELCOME = shots_root() / "welcome_onbo.png"
SHOT_SIGNIN = shots_root() / "sign-in.png"
SHOT_SIGNUP = shots_root() / "signup.png"
SHOT_HOME = shots_root() / "home-dash.png"
SHOT_ACTIVITY = shots_root() / "activity.png"
SHOT_ALARM = shots_root() / "smart_alarn.png"
SHOT_ANALYTICS = shots_root() / "sleep_analysis.png"
SHOT_COACH = shots_root() / "sleep-coach.png"
SHOT_PROFILE = shots_root() / "profile.png"


def add_background(slide: Slide) -> None:
    slide.rect(0, 0, SLIDE_W, SLIDE_H, fill=BG, name="Bg")
    slide.ellipse(emu(8.8), emu(-0.6), emu(4.3), emu(3.0), fill=PRIMARY, fill_alpha=12000, line=None, name="GlowOne")
    slide.ellipse(emu(-0.9), emu(4.0), emu(4.6), emu(3.0), fill=ACCENT, fill_alpha=7000, line=None, name="GlowTwo")
    slide.ellipse(emu(5.8), emu(5.6), emu(3.2), emu(1.6), fill=PRIMARY_SOFT, fill_alpha=7000, line=None, name="GlowThree")


def add_title_block(slide: Slide, title: str, subtitle: str) -> None:
    slide.text(emu(0.8), emu(0.6), emu(5.8), emu(0.8), title, color=TEXT, size=28, bold=True, font="Aptos Display")
    slide.text(emu(0.82), emu(1.25), emu(5.4), emu(0.9), subtitle, color=MUTED, size=13.5)


def feature_chip(slide: Slide, x: float, y: float, label: str, color: str) -> None:
    slide.rect(
        emu(x),
        emu(y),
        emu(1.6),
        emu(0.42),
        fill=CARD_ALT,
        line=color,
        line_alpha=45000,
        round_rect=True,
        text=label,
        text_color=TEXT,
        text_size=11,
        bold=False,
        align="ctr",
        name="Chip",
    )


def phone_with_caption(slide: Slide, x: float, y: float, w: float, h: float, caption: str, builder) -> None:
    phone = PhoneCanvas(slide, emu(x), emu(y), emu(w), emu(h))
    builder(phone)
    slide.text(emu(x), emu(y + h + 0.08), emu(w), emu(0.28), caption, color=MUTED, size=9.5, align="ctr")


def screen_welcome(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.circle(320, 190, 360, 360, fill=PRIMARY, fill_alpha=18000, line=None)
    phone.circle(392, 262, 216, 216, fill=PRIMARY, fill_alpha=26000, line=None, text="DM", text_color=TEXT, text_size=34, bold=True, name="Logo")
    phone.label(160, 630, 680, 140, "Restorative Sleep", size=25, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    phone.label(140, 790, 720, 180, "Discover calming routines,\nsmart tracking, and deeper rest.", size=13, color=MUTED, align="ctr")
    phone.circle(410, 1450, 24, 24, fill=BORDER, fill_alpha=60000, line=None)
    phone.rr(455, 1450, 80, 24, fill=PRIMARY, line=None)
    phone.circle(555, 1450, 24, 24, fill=BORDER, fill_alpha=60000, line=None)
    phone.rr(120, 1600, 760, 120, fill=PRIMARY, line=None, text="Get Started", text_color=TEXT, text_size=17, bold=True, align="ctr", valign="ctr")


def screen_goal(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.circle(382, 170, 236, 236, fill=PRIMARY, fill_alpha=18000, line=None)
    phone.circle(435, 223, 130, 130, fill=CARD_ALT, line=PRIMARY, text="GO", text_color=PRIMARY_SOFT, text_size=20, bold=True)
    phone.label(150, 480, 700, 120, "What is your goal?", size=24, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    cards = [
        ("Fall Asleep Faster", True, PRIMARY),
        ("Wake Up Refreshed", False, ACCENT),
        ("Track Sleep Health", False, PRIMARY_SOFT),
    ]
    top = 700
    for idx, (label, selected, accent) in enumerate(cards):
        y = top + idx * 230
        phone.rr(
            90,
            y,
            820,
            170,
            fill=CARD,
            line=PRIMARY if selected else BORDER,
            line_alpha=70000 if selected else 30000,
            text=label,
            text_color=TEXT,
            text_size=15,
            bold=True,
            name="GoalCard",
        )
        phone.circle(130, y + 47, 72, 72, fill=CARD_ALT if not selected else PRIMARY, fill_alpha=100000 if selected else 85000, line=None, text="o", text_color=TEXT if selected else accent, text_size=24, bold=True)
    phone.rr(120, 1620, 760, 120, fill=PRIMARY, line=None, text="Continue", text_color=TEXT, text_size=17, bold=True, align="ctr")


def screen_schedule(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(150, 200, 700, 120, "Ideal Schedule", size=24, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    phone.label(130, 335, 740, 120, "Set your sleep window to calibrate smart guidance.", size=12.5, color=MUTED, align="ctr")
    phone.rr(110, 600, 360, 310, fill=CARD, line=BORDER, line_alpha=35000, text="Bedtime\n10:30 PM", text_color=TEXT, text_size=17, bold=True, align="ctr")
    phone.rr(530, 600, 360, 310, fill=CARD, line=BORDER, line_alpha=35000, text="Wake Up\n06:45 AM", text_color=TEXT, text_size=17, bold=True, align="ctr")
    phone.rr(250, 1010, 500, 88, fill=CARD_ALT, line=BORDER, line_alpha=25000, text="8h 15m total rest", text_color=TEXT, text_size=12.5, align="ctr")
    phone.rr(120, 1600, 760, 120, fill=PRIMARY, line=None, text="Continue", text_color=TEXT, text_size=17, bold=True, align="ctr")


def screen_sync(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.circle(334, 180, 332, 332, fill=PRIMARY, fill_alpha=17000, line=None)
    phone.circle(412, 258, 176, 176, fill=CARD_ALT, line=PRIMARY, text="AI", text_color=PRIMARY_SOFT, text_size=24, bold=True)
    phone.label(190, 560, 620, 120, "Sync your vitals", size=24, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    phone.label(120, 700, 760, 190, "Connect health data so DreamMind can personalize sleep insights and automate tracking.", size=12.5, color=MUTED, align="ctr")
    phone.rr(170, 980, 660, 80, fill=CARD_ALT, line=BORDER, line_alpha=25000, text="Privacy is our priority", text_color=TEXT, text_size=11.5, align="ctr")
    phone.rr(120, 1520, 760, 120, fill=PRIMARY, line=None, text="Enable Sync", text_color=TEXT, text_size=16.5, bold=True, align="ctr")
    phone.label(320, 1670, 360, 50, "Skip for now", size=11.5, color=MUTED, align="ctr")


def screen_home(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(90, 110, 650, 70, "Good evening, Alex", size=16.5, color=TEXT, bold=True, font="Aptos Display")
    phone.label(90, 175, 500, 50, "Ready for a restful night?", size=9.8, color=MUTED)
    phone.circle(790, 110, 110, 110, fill=CARD_ALT, line=BORDER, text="A", text_color=TEXT, text_size=18, bold=True)
    phone.rr(80, 300, 840, 360, fill=CARD, line=BORDER, line_alpha=32000, name="HeroCard")
    phone.label(130, 350, 420, 60, "Last Night's Sleep", size=8.8, color=MUTED)
    phone.label(130, 410, 360, 95, "7h 24m", size=24, color=TEXT, bold=True, font="Aptos Display")
    phone.rr(670, 360, 180, 72, fill=PRIMARY, fill_alpha=24000, line=PRIMARY, line_alpha=25000, text="82/100", text_color=PRIMARY_SOFT, text_size=11, bold=True, align="ctr")
    for i, h in enumerate([80, 120, 60, 150, 180, 110, 75, 130]):
        phone.rect(140 + i * 72, 540 - h, 42, h, fill=PRIMARY, fill_alpha=22000 + i * 8000, line=None)
    for idx, label in enumerate(["Log", "Alarm", "Coach"]):
        x = 80 + idx * 286
        phone.rr(x, 720, 250, 220, fill=CARD_ALT, line=BORDER, line_alpha=28000, text=label, text_color=TEXT, text_size=11.5, bold=True, align="ctr")
    phone.rr(80, 990, 840, 180, fill=CARD, line=BORDER, line_alpha=25000, text="AI Insight\nHerbal tea before bed could improve deep sleep by 12%.", text_color=TEXT, text_size=11.5, bold=True)
    phone.label(90, 1220, 480, 50, "Nightly Routine", size=12, color=TEXT, bold=True)
    phone.label(720, 1220, 160, 50, "View all", size=9.8, color=PRIMARY_SOFT, align="r")
    phone.rr(80, 1290, 840, 120, fill=CARD_ALT, line=BORDER, line_alpha=25000, text="Breathing Exercise", text_color=TEXT, text_size=11.5, bold=True)
    phone.rr(80, 1445, 840, 120, fill=CARD_ALT, line=BORDER, line_alpha=25000, text="Deep Rain Ambience", text_color=TEXT, text_size=11.5, bold=True)
    phone.bottom_nav(0)


def screen_sleep_log(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(95, 110, 480, 50, "Tonight's Log", size=15.5, color=TEXT, bold=True, font="Aptos Display")
    phone.rr(80, 210, 370, 200, fill=CARD, line=BORDER, line_alpha=25000, text="Bedtime\n10:45 PM", text_color=TEXT, text_size=13.5, bold=True, align="ctr")
    phone.rr(550, 210, 370, 200, fill=CARD, line=BORDER, line_alpha=25000, text="Wake Up\n07:30 AM", text_color=TEXT, text_size=13.5, bold=True, align="ctr")
    phone.rr(80, 470, 840, 240, fill=CARD, line=BORDER, line_alpha=25000, text="Sleep Quality\nFair", text_color=TEXT, text_size=13.5, bold=True)
    phone.rect(140, 640, 660, 26, fill=CARD_ALT, line=None)
    phone.rect(140, 640, 420, 26, fill=PRIMARY, line=None)
    phone.circle(540, 628, 38, 38, fill=TEXT, line=PRIMARY)
    phone.label(90, 780, 500, 50, "Pre-sleep Activities", size=11.5, color=TEXT, bold=True)
    tags = ["Caffeine", "Reading", "Exercise", "Meditation", "+ Add"]
    for idx, tag in enumerate(tags):
        x = 80 + (idx % 2) * 420
        y = 860 + (idx // 2) * 120
        fill = PRIMARY if tag == "Exercise" else CARD_ALT
        fill_alpha = 17000 if tag == "Exercise" else 100000
        phone.rr(x, y, 360, 88, fill=fill, fill_alpha=fill_alpha, line=BORDER, line_alpha=25000, text=tag, text_color=TEXT if tag != "Exercise" else PRIMARY_SOFT, text_size=10.8, bold=tag == "Exercise", align="ctr")
    phone.rr(80, 1240, 840, 170, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Notes\nKept the room cool and avoided late screens.", text_color=TEXT, text_size=10.8)
    phone.rr(120, 1600, 760, 120, fill=PRIMARY, line=None, text="Save Sleep Entry", text_color=TEXT, text_size=16.5, bold=True, align="ctr")


def screen_alarm(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(100, 110, 460, 55, "Smart Alarm", size=15.5, color=TEXT, bold=True, font="Aptos Display")
    phone.label(100, 170, 650, 45, "Alarm set for 6:45 AM", size=9.5, color=MUTED)
    phone.circle(210, 300, 580, 580, fill=CARD, line=PRIMARY, line_alpha=35000)
    phone.circle(270, 360, 460, 460, fill=BG, line=BORDER, line_alpha=30000)
    phone.label(320, 515, 360, 120, "06:45", size=27, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    phone.label(410, 645, 180, 45, "AM", size=11, color=PRIMARY_SOFT, bold=True, align="ctr")
    phone.rr(90, 930, 820, 180, fill=CARD, line=BORDER, line_alpha=25000, text="Smart Wake\nLightest sleep phase within a 30-minute window.", text_color=TEXT, text_size=11.2, bold=True)
    phone.rr(90, 1160, 820, 100, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Forest Morning", text_color=TEXT, text_size=11.2, bold=True)
    phone.rr(90, 1290, 820, 100, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Ocean Swell", text_color=MUTED, text_size=11.0)
    phone.rr(90, 1430, 820, 80, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Vibration: Strong", text_color=TEXT, text_size=10.8)
    phone.rr(120, 1600, 760, 120, fill=PRIMARY, line=None, text="Save Alarm", text_color=TEXT, text_size=16.5, bold=True, align="ctr")
    phone.bottom_nav(1)


def screen_analytics(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(100, 110, 500, 55, "Sleep Analytics", size=15.5, color=TEXT, bold=True, font="Aptos Display")
    phone.circle(790, 108, 110, 110, fill=CARD_ALT, line=BORDER, text="A", text_color=TEXT, text_size=18, bold=True)
    phone.rr(100, 240, 800, 90, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Week     Month     Year", text_color=MUTED, text_size=10.5, align="ctr")
    phone.rr(80, 390, 840, 340, fill=CARD, line=BORDER, line_alpha=25000, text="Sleep Hours\n7h 48m avg", text_color=TEXT, text_size=12.2, bold=True)
    for idx, width in enumerate([540, 640, 430, 690, 520]):
        y = 515 + idx * 40
        phone.rect(180, y, 560, 16, fill=CARD_ALT, line=None)
        phone.rect(180, y, width, 16, fill=PRIMARY, line=None)
    phone.rr(80, 780, 840, 360, fill=CARD, line=BORDER, line_alpha=25000, name="QualityCard")
    phone.circle(300, 885, 400, 400, fill=CARD_ALT, line=PRIMARY, line_alpha=35000)
    phone.label(360, 1010, 280, 70, "85%", size=26, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    phone.label(385, 1080, 220, 40, "Quality", size=10.5, color=MUTED, align="ctr")
    stats = [("Avg Bedtime", "11:24 PM"), ("Avg Wake", "7:12 AM"), ("Debt", "-1h 15m"), ("Consistency", "92%")]
    for idx, (label, value) in enumerate(stats):
        x = 80 + (idx % 2) * 430
        y = 1190 + (idx // 2) * 140
        phone.rr(x, y, 390, 110, fill=CARD_ALT, line=BORDER, line_alpha=22000, text=f"{label}\n{value}", text_color=TEXT, text_size=10.2, bold=True)
    phone.bottom_nav(2)


def screen_coach(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(300, 110, 400, 55, "Sleep Coach", size=15.5, color=TEXT, bold=True, align="ctr", font="Aptos Display")
    phone.label(260, 170, 480, 40, "Always here to help you rest", size=9.2, color=MUTED, align="ctr")
    phone.rr(80, 300, 700, 170, fill=PRIMARY, fill_alpha=13000, line=PRIMARY, line_alpha=16000, text="Good morning. Your deep sleep was 15% higher than your average.", text_color=TEXT, text_size=11.0)
    phone.label(86, 478, 180, 32, "08:32 AM", size=7.6, color=MUTED)
    phone.rr(220, 560, 700, 155, fill=CARD_ALT, line=BORDER, line_alpha=25000, text="I feel refreshed. Can you analyze why my sleep improved?", text_color=TEXT, text_size=10.8)
    phone.label(730, 722, 150, 32, "08:34 AM", size=7.6, color=MUTED, align="r")
    phone.rr(80, 810, 780, 220, fill=PRIMARY, fill_alpha=13000, line=PRIMARY, line_alpha=16000, text="Earlier bedtime and steadier HRV likely improved recovery. Your cycles looked more balanced.", text_color=TEXT, text_size=10.7)
    chips = ["Analyze last night", "Bedtime tips", "Adjust goals"]
    for idx, chip in enumerate(chips):
        phone.rr(80 + idx * 270, 1110, 240, 74, fill=CARD_ALT, line=BORDER, line_alpha=22000, text=chip, text_color=TEXT, text_size=9.0, align="ctr")
    phone.rr(80, 1620, 840, 96, fill=CARD, line=BORDER, line_alpha=22000, text="Ask your coach anything...", text_color=MUTED, text_size=10.8)
    phone.circle(820, 1632, 70, 70, fill=PRIMARY, line=None, text=">", text_color=TEXT, text_size=18, bold=True)
    phone.bottom_nav(3)


def screen_profile(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(100, 110, 300, 55, "Profile", size=15.5, color=TEXT, bold=True, font="Aptos Display")
    phone.circle(790, 108, 110, 110, fill=CARD_ALT, line=BORDER, text="S", text_color=MUTED, text_size=16, bold=True)
    phone.rr(80, 250, 840, 430, fill=CARD, line=BORDER, line_alpha=25000, name="ProfileHero")
    phone.circle(390, 310, 220, 220, fill=PRIMARY, line=PRIMARY_SOFT, text="A", text_color=TEXT, text_size=30, bold=True)
    phone.label(260, 560, 480, 55, "Alex Johnson", size=16, color=TEXT, bold=True, align="ctr")
    phone.label(210, 620, 580, 45, "Pro Sleeper - Member since Jan 2024", size=9.0, color=MUTED, align="ctr")
    for idx, item in enumerate([("14", "Day Streak"), ("8.2h", "Avg Sleep"), ("94%", "Consistency")]):
        x = 140 + idx * 245
        phone.label(x, 705, 160, 48, item[0], size=13.5, color=TEXT, bold=True, align="ctr")
        phone.label(x, 752, 160, 34, item[1], size=8.2, color=MUTED, align="ctr")
    rows = ["Personal Information", "Bedtime Reminders", "Connected Devices", "Privacy Policy"]
    for idx, row in enumerate(rows):
        phone.rr(80, 760 + idx * 145, 840, 110, fill=CARD_ALT, line=BORDER, line_alpha=22000, text=row, text_color=TEXT, text_size=10.8, bold=True)
    phone.rr(220, 1590, 560, 96, fill=CARD_ALT, line=DANGER, line_alpha=35000, text="Log Out", text_color=DANGER, text_size=12.5, bold=True, align="ctr")
    phone.bottom_nav(4)


def screen_detail(phone: PhoneCanvas) -> None:
    phone.dot_bg()
    phone.label(90, 110, 360, 45, "Sleep Session", size=14.8, color=TEXT, bold=True, font="Aptos Display")
    phone.rr(700, 100, 180, 52, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Oct 24", text_color=MUTED, text_size=9.0, align="ctr")
    phone.label(90, 240, 280, 45, "Total Duration", size=8.8, color=MUTED)
    phone.label(90, 290, 320, 90, "7h 24m", size=22, color=TEXT, bold=True, font="Aptos Display")
    phone.rr(660, 270, 190, 72, fill=PRIMARY, fill_alpha=22000, line=PRIMARY, line_alpha=22000, text="82/100", text_color=PRIMARY_SOFT, text_size=11, bold=True, align="ctr")
    phone.rr(80, 430, 840, 420, fill=CARD, line=BORDER, line_alpha=22000, text="Sleep Architecture", text_color=TEXT, text_size=11.2, bold=True)
    heights = [180, 110, 160, 80, 200, 100, 150]
    kinds = [PINK, PRIMARY_SOFT, PRIMARY, ACCENT, PRIMARY, PRIMARY_SOFT, PINK]
    for idx, h in enumerate(heights):
        phone.rect(130 + idx * 95, 760 - h, 48, h, fill=kinds[idx], line=None)
    phone.rr(80, 910, 840, 180, fill=CARD, line=PRIMARY, line_alpha=22000, text="Coach Insight\nLower screen time may have improved recovery and REM balance.", text_color=TEXT, text_size=10.8, bold=True)
    phone.rr(80, 1140, 390, 120, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Deep Sleep\n1h 12m", text_color=TEXT, text_size=10.5, bold=True)
    phone.rr(530, 1140, 390, 120, fill=CARD_ALT, line=BORDER, line_alpha=22000, text="Efficiency\n94%", text_color=TEXT, text_size=10.5, bold=True)
    phone.rr(80, 1300, 840, 90, fill=CARD_ALT, line=BORDER, line_alpha=20000, text="Activities: Reading - Meditation", text_color=TEXT, text_size=9.8)


def add_card(slide: Slide, x: float, y: float, w: float, h: float, title: str, body: str, accent: str) -> None:
    slide.rect(
        emu(x),
        emu(y),
        emu(w),
        emu(h),
        fill=CARD,
        line=BORDER,
        line_alpha=35000,
        round_rect=True,
        name="InfoCard",
    )
    slide.rect(emu(x + 0.18), emu(y + 0.18), emu(0.12), emu(h - 0.36), fill=accent, fill_alpha=100000, round_rect=True, name="AccentBar")
    slide.text(emu(x + 0.42), emu(y + 0.25), emu(w - 0.65), emu(0.35), title, color=TEXT, size=13.5, bold=True)
    slide.text(emu(x + 0.42), emu(y + 0.62), emu(w - 0.62), emu(h - 0.8), body, color=MUTED, size=10.8)


def make_slide_1() -> Slide:
    slide = Slide("DreamMind Cover")
    add_background(slide)
    slide.text(emu(0.85), emu(0.92), emu(5.6), emu(0.9), "DreamMind", color=TEXT, size=31, bold=True, font="Aptos Display")
    slide.text(emu(0.9), emu(1.72), emu(5.4), emu(1.15), "A smart sleep wellness app for better routines, clearer analytics, and personalized recovery guidance.", color=MUTED, size=15)
    feature_chip(slide, 0.92, 3.0, "Sleep Logging", PRIMARY)
    feature_chip(slide, 2.68, 3.0, "Smart Alarm", ACCENT)
    feature_chip(slide, 4.44, 3.0, "AI Coach", PRIMARY_SOFT)
    slide.text(emu(0.92), emu(6.15), emu(4.2), emu(0.32), "Final Exam Presentation", color=MUTED, size=10.5)
    add_screenshot_card(slide, SHOT_HOME, 7.2, 0.72, 3.15, 5.45, "Home Dashboard", "Real DreamMind screenshot", PRIMARY)
    add_screenshot_card(slide, SHOT_COACH, 10.45, 1.35, 2.1, 4.15, "AI Coach", "", ACCENT)
    return slide


def make_slide_2() -> Slide:
    slide = Slide("Introduction and Team")
    add_background(slide)
    add_title_block(slide, "Introduction and Team", "DreamMind is a mobile sleep wellness app designed for students and busy users who want better routines, clearer sleep insight, and calmer daily habits.")
    members = [
        ("Xalilov Abdukarim", "ID: 230544", "Concept and presentation structure", PRIMARY, 0.92, 2.0),
        ("Tohirov Hikmatillo", "ID: 230272", "Android implementation and architecture", ACCENT, 6.45, 2.0),
        ("Choriyev Mirjalol", "ID: 230234", "UI/UX design and user flow", PRIMARY_SOFT, 0.92, 4.1),
        ("Bo'taboyev Sherzod", "ID: 230804", "Testing review and future planning", PRIMARY, 6.45, 4.1),
    ]
    for name, student_id, role, accent, x, y in members:
        slide.rect(emu(x), emu(y), emu(5.0), emu(1.52), fill=CARD, line=accent, line_alpha=40000, round_rect=True, name="TeamMember")
        slide.rect(emu(x + 0.18), emu(y + 0.18), emu(0.12), emu(1.16), fill=accent, round_rect=True, name="AccentBar")
        slide.text(emu(x + 0.42), emu(y + 0.2), emu(2.95), emu(0.28), name, color=TEXT, size=15, bold=True)
        slide.text(emu(x + 3.33), emu(y + 0.22), emu(1.28), emu(0.24), student_id, color=MUTED, size=10.5, bold=True, align="r")
        slide.text(emu(x + 0.42), emu(y + 0.62), emu(4.18), emu(0.44), role, color=MUTED, size=10.8)
    return slide


def make_slide_3() -> Slide:
    slide = Slide("Problem Motivation Target Users")
    add_background(slide)
    add_title_block(slide, "Problem, Motivation, and Target Users", "The project starts from a real user pain point: sleep support is often fragmented, hard to interpret, and not engaging enough for daily use.")
    add_card(slide, 0.88, 2.0, 3.55, 1.2, "Problem", "Users often rely on separate apps for tracking, alarms, and advice, which weakens routine consistency.", PRIMARY)
    add_card(slide, 0.88, 3.42, 3.55, 1.2, "Motivation", "DreamMind unifies those tasks inside one calm interface that encourages regular use and easier interpretation.", ACCENT)
    add_card(slide, 0.88, 4.84, 3.55, 1.2, "Target users", "Students, young professionals, and wellness-focused users who need sleep guidance without complexity.", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_WELCOME, 5.15, 1.95, 2.25, 4.55, "Welcome", "First emotional entry into the app", PRIMARY)
    add_screenshot_card(slide, SHOT_HOME, 7.72, 1.75, 2.4, 4.75, "Home Dashboard", "Key sleep insights at a glance", ACCENT)
    add_screenshot_card(slide, SHOT_ANALYTICS, 10.43, 1.95, 2.05, 4.55, "Analytics", "Data becomes understandable trends", PRIMARY_SOFT)
    return slide


def make_slide_4() -> Slide:
    slide = Slide("Goals and Features")
    add_background(slide)
    add_title_block(slide, "Application Goals and Core Functionalities", "DreamMind is built to guide users from first-time onboarding to daily sleep improvement through one connected feature set.")
    add_card(slide, 0.9, 2.02, 3.48, 1.16, "Goal 1", "Create one place for sleep onboarding, dashboard insight, and routine building.", PRIMARY)
    add_card(slide, 0.9, 3.38, 3.48, 1.16, "Goal 2", "Turn sleep behaviour into actionable information through logging, alarms, and analytics.", ACCENT)
    add_card(slide, 0.9, 4.74, 3.48, 1.16, "Goal 3", "Support engagement with coaching, personalized screens, and premium UI consistency.", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_HOME, 5.05, 1.92, 2.0, 2.15, "Dashboard", "", PRIMARY)
    add_screenshot_card(slide, SHOT_ACTIVITY, 7.35, 1.92, 2.0, 2.15, "Sleep Log", "", ACCENT)
    add_screenshot_card(slide, SHOT_ALARM, 9.65, 1.92, 2.0, 2.15, "Smart Alarm", "", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_COACH, 5.05, 4.2, 2.0, 2.15, "AI Coach", "", PRIMARY)
    add_screenshot_card(slide, SHOT_ANALYTICS, 7.35, 4.2, 2.0, 2.15, "Analytics", "", ACCENT)
    add_screenshot_card(slide, SHOT_PROFILE, 9.65, 4.2, 2.0, 2.15, "Profile", "", PRIMARY_SOFT)
    return slide


def make_slide_5() -> Slide:
    slide = Slide("Development Tools")
    add_background(slide)
    add_title_block(slide, "Development Tools and Stack", "The project uses a modern Android stack chosen for reactive UI, maintainable architecture, and API-based feature growth.")
    add_card(slide, 0.92, 2.0, 2.9, 1.45, "IDE and Language", "Android Studio\nKotlin", PRIMARY)
    add_card(slide, 3.95, 2.0, 2.9, 1.45, "UI Framework", "Jetpack Compose\nMaterial 3\nNavigation Compose", ACCENT)
    add_card(slide, 6.98, 2.0, 2.9, 1.45, "State and Storage", "ViewModel\nStateFlow\nDataStore Preferences", PRIMARY_SOFT)
    add_card(slide, 10.01, 2.0, 2.2, 1.45, "Networking", "Retrofit\nGson\nOkHttp", PRIMARY)
    add_card(slide, 1.85, 4.1, 3.9, 1.45, "Workflow Tools", "Git and GitHub for collaboration, version control, and final submission packaging.", ACCENT)
    add_card(slide, 6.15, 4.1, 4.25, 1.45, "Project Reasoning", "This stack supports fast UI iteration, screen-level state handling, and easier separation between interface and data logic.", PRIMARY_SOFT)
    slide.text(emu(1.05), emu(6.03), emu(10.9), emu(0.3), "The stack also aligns well with the course focus on Kotlin mobile development and clean architectural thinking.", color=TEXT, size=11.2, bold=True)
    return slide


def make_slide_6() -> Slide:
    slide = Slide("Architecture")
    add_background(slide)
    add_title_block(slide, "Architecture Overview", "DreamMind follows an MVVM-oriented structure with clear layers for UI, state, repositories, navigation, and network or local services.")
    for idx, (label, body, color) in enumerate(
        [
            ("Compose UI", "Screens render user interaction and visual state.", PRIMARY),
            ("Feature ViewModels", "StateFlow-based state and event handling per screen.", ACCENT),
            ("Repositories", "Business logic connects screens to remote and fallback data.", PRIMARY_SOFT),
            ("Services", "Retrofit API, DataStore tokens, and local alarm notifications.", PRIMARY),
        ]
    ):
        y = 2.0 + idx * 1.02
        slide.rect(emu(0.95), emu(y), emu(4.8), emu(0.75), fill=CARD, line=color, line_alpha=40000, round_rect=True, name="Layer")
        slide.text(emu(1.18), emu(y + 0.12), emu(1.55), emu(0.22), label, color=TEXT, size=13.5, bold=True)
        slide.text(emu(2.95), emu(y + 0.1), emu(2.45), emu(0.3), body, color=MUTED, size=10.2)
    add_screenshot_card(slide, SHOT_HOME, 6.05, 1.95, 2.0, 3.95, "Home UI", "", PRIMARY)
    add_screenshot_card(slide, SHOT_ANALYTICS, 8.35, 1.95, 2.0, 3.95, "Analytics UI", "", ACCENT)
    add_screenshot_card(slide, SHOT_COACH, 10.65, 1.95, 2.0, 3.95, "Coach UI", "", PRIMARY_SOFT)
    slide.text(emu(6.05), emu(6.12), emu(6.4), emu(0.28), "Representative flow: User action -> ViewModel -> Repository -> API / Local service -> Updated UI state", color=TEXT, size=11.2, bold=True)
    return slide


def make_slide_7() -> Slide:
    slide = Slide("Design Highlights")
    add_background(slide)
    add_title_block(slide, "UI/UX Design Highlights", "The design language aims to feel calm, premium, and easy to navigate while keeping sleep metrics and actions visually clear.")
    add_screenshot_card(slide, SHOT_WELCOME, 0.95, 2.02, 2.05, 4.35, "Welcome", "", PRIMARY)
    add_screenshot_card(slide, SHOT_HOME, 3.25, 2.02, 2.05, 4.35, "Dashboard", "", ACCENT)
    add_screenshot_card(slide, SHOT_ANALYTICS, 5.55, 2.02, 2.05, 4.35, "Analytics", "", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_COACH, 7.85, 2.02, 2.05, 4.35, "Coach", "", PRIMARY)
    add_card(slide, 10.15, 2.0, 2.25, 1.0, "Palette", "Dark night palette with soft accent glow.", ACCENT)
    add_card(slide, 10.15, 3.18, 2.25, 1.0, "Hierarchy", "Large cards highlight the most important metrics first.", PRIMARY_SOFT)
    add_card(slide, 10.15, 4.36, 2.25, 1.0, "Flow", "Onboarding and core navigation stay simple and guided.", PRIMARY)
    add_card(slide, 10.15, 5.54, 2.25, 1.0, "Consistency", "Reusable card shapes and spacing unify the app.", ACCENT)
    return slide


def make_slide_8() -> Slide:
    slide = Slide("Main Screens and User Flow")
    add_background(slide)
    add_title_block(slide, "Main Screens and User Flow", "The presentation demo follows a simple flow from entry to daily usage: authentication, dashboard, data capture, insights, and support.")
    add_screenshot_card(slide, SHOT_SIGNIN, 0.92, 2.0, 2.0, 4.35, "Sign In", "Returning user entry", PRIMARY)
    add_screenshot_card(slide, SHOT_HOME, 3.23, 1.82, 2.15, 4.55, "Home", "Immediate overview and actions", ACCENT)
    add_screenshot_card(slide, SHOT_ACTIVITY, 5.68, 2.0, 2.0, 4.35, "Sleep Log", "Capture behaviour and quality", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_ALARM, 7.99, 2.0, 2.0, 4.35, "Alarm", "Plan the wake-up routine", PRIMARY)
    add_screenshot_card(slide, SHOT_ANALYTICS, 10.3, 2.0, 2.0, 4.35, "Analytics", "Review trends and results", ACCENT)
    slide.text(emu(1.05), emu(6.05), emu(11.0), emu(0.28), "Presentation demo flow: Sign In -> Home -> Sleep Log -> Smart Alarm -> Analytics -> Coach -> Profile", color=TEXT, size=11.3, bold=True)
    return slide


def make_slide_9() -> Slide:
    slide = Slide("Challenges and Solutions")
    add_background(slide)
    add_title_block(slide, "Challenges and Solutions", "The project required both design and engineering tradeoffs, especially when moving from polished mockups into a working Compose application.")
    items = [
        ("UI consistency", "Challenge: keep the HTML-inspired premium look inside Compose.\nSolution: reusable design-system cards, buttons, and shared color tokens.", PRIMARY, 0.92, 2.0),
        ("State management", "Challenge: coordinate auth, onboarding, and feature states cleanly.\nSolution: screen-specific ViewModels with StateFlow-based UI state.", ACCENT, 6.45, 2.0),
        ("Data reliability", "Challenge: support remote data without breaking demo stability.\nSolution: repositories combine API calls with fallback demo content.", PRIMARY_SOFT, 0.92, 4.15),
        ("Alarm realism", "Challenge: make the alarm flow more than a static settings screen.\nSolution: local scheduling plus notification receiver support.", PRIMARY, 6.45, 4.15),
    ]
    for title, body, accent, x, y in items:
        slide.rect(emu(x), emu(y), emu(5.0), emu(1.6), fill=CARD, line=accent, line_alpha=40000, round_rect=True, name="Challenge")
        slide.rect(emu(x + 0.18), emu(y + 0.18), emu(0.12), emu(1.24), fill=accent, round_rect=True, name="AccentBar")
        slide.text(emu(x + 0.42), emu(y + 0.2), emu(3.8), emu(0.28), title, color=TEXT, size=15, bold=True)
        slide.text(emu(x + 0.42), emu(y + 0.6), emu(4.18), emu(0.7), body, color=MUTED, size=10.2)
    return slide


def make_slide_10() -> Slide:
    slide = Slide("Development Timeline")
    add_background(slide)
    add_title_block(slide, "Development Timeline", "The project progressed through clear milestones from problem framing to design, implementation, integration, and presentation readiness.")
    milestones = [
        ("1", "Research", "Defined the sleep problem, target audience, and app scope.", PRIMARY, 0.72),
        ("2", "Design", "Created the DreamMind screen system and visual identity.", ACCENT, 3.12),
        ("3", "Core Build", "Implemented authentication, onboarding, and dashboard structure.", PRIMARY_SOFT, 5.52),
        ("4", "Integration", "Added logging, alarm, analytics, coach, and profile flows.", PRIMARY, 7.92),
        ("5", "Polish", "Refined UI, prepared demo order, and finalized presentation assets.", ACCENT, 10.32),
    ]
    for number, title, body, accent, x in milestones:
        slide.rect(emu(x), emu(2.45), emu(2.05), emu(2.55), fill=CARD, line=accent, line_alpha=40000, round_rect=True, name="Milestone")
        slide.ellipse(emu(x + 0.76), emu(2.1), emu(0.52), emu(0.52), fill=accent, line=None, text=number, text_color=BG, text_size=12.5, bold=True, name="StepDot")
        slide.text(emu(x + 0.22), emu(2.9), emu(1.6), emu(0.28), title, color=TEXT, size=14.2, bold=True, align="ctr")
        slide.text(emu(x + 0.18), emu(3.35), emu(1.7), emu(1.0), body, color=MUTED, size=9.8, align="ctr")
    slide.text(emu(0.95), emu(5.6), emu(11.2), emu(0.32), "This structure helps explain the project as a real development process, not only a collection of screens.", color=TEXT, size=11.1, bold=True, align="ctr")
    return slide


def make_slide_11() -> Slide:
    slide = Slide("Future Work")
    add_background(slide)
    add_title_block(slide, "Future Work and Scalability", "The current architecture leaves room for stronger production behaviour, deeper personalization, and broader device integration.")
    add_card(slide, 0.92, 2.0, 3.0, 1.4, "Backend growth", "Expand remote sync, stronger account flows, and richer analytics endpoints.", PRIMARY)
    add_card(slide, 4.15, 2.0, 3.0, 1.4, "Offline support", "Add local persistence for history, caching, and better resilience without network.", ACCENT)
    add_card(slide, 7.38, 2.0, 3.0, 1.4, "Smarter guidance", "Improve coaching recommendations with more personalized sleep patterns.", PRIMARY_SOFT)
    add_card(slide, 10.61, 2.0, 1.8, 1.4, "Testing", "Increase automated coverage and validation depth.", PRIMARY)
    add_card(slide, 1.65, 4.15, 4.0, 1.45, "Scalability direction", "The repository-based structure makes it easier to add new features without tightly coupling screens and data sources.", ACCENT)
    add_card(slide, 6.05, 4.15, 4.6, 1.45, "Possible extensions", "Wearable integration, sleep recommendations from device sensors, and richer session comparison views.", PRIMARY_SOFT)
    slide.text(emu(1.05), emu(6.02), emu(10.8), emu(0.28), "This slide directly answers the assignment requirement about planned enhancements and future scalability.", color=TEXT, size=11.1, bold=True, align="ctr")
    return slide


def make_slide_12() -> Slide:
    slide = Slide("Conclusion and Demo")
    add_background(slide)
    add_title_block(slide, "Conclusion and Live Demo Plan", "DreamMind is best presented as a visually polished Kotlin app with meaningful user value, a clear architecture, and a focused demonstration flow.")
    add_card(slide, 0.92, 2.0, 3.3, 1.2, "Key message", "DreamMind is more than a concept. It combines product thinking, design quality, and working mobile app structure.", PRIMARY)
    add_card(slide, 0.92, 3.45, 3.3, 1.2, "Demo order", "Sign In -> Home -> Sleep Log -> Smart Alarm -> Analytics -> Coach -> Profile", ACCENT)
    add_card(slide, 0.92, 4.9, 3.3, 1.2, "Closing note", "The project demonstrates how one app can connect guidance, data, and calm user experience around sleep wellness.", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_HOME, 4.7, 1.95, 2.0, 4.35, "Dashboard", "", PRIMARY)
    add_screenshot_card(slide, SHOT_ALARM, 6.95, 1.95, 2.0, 4.35, "Alarm", "", ACCENT)
    add_screenshot_card(slide, SHOT_ANALYTICS, 9.2, 1.95, 2.0, 4.35, "Analytics", "", PRIMARY_SOFT)
    add_screenshot_card(slide, SHOT_COACH, 11.45, 1.95, 1.2, 4.35, "Coach", "", PRIMARY)
    return slide


def make_slide_13() -> Slide:
    slide = Slide("Screens Gallery")
    add_background(slide)
    add_title_block(slide, "Screens Gallery", "Real uploaded DreamMind screenshots used as backup visual proof during Q&A or when examiners ask to see more screens quickly.")
    gallery = [
        (SHOT_WELCOME, "Welcome", PRIMARY, 0.88, 1.9, 1.65, 2.2),
        (SHOT_SIGNUP, "Sign Up", ACCENT, 2.8, 1.9, 1.65, 2.2),
        (SHOT_SIGNIN, "Sign In", PRIMARY_SOFT, 4.72, 1.9, 1.65, 2.2),
        (SHOT_HOME, "Home", PRIMARY, 6.64, 1.9, 1.65, 2.2),
        (SHOT_ACTIVITY, "Activity", ACCENT, 8.56, 1.9, 1.65, 2.2),
        (SHOT_ALARM, "Alarm", PRIMARY_SOFT, 10.48, 1.9, 1.65, 2.2),
        (SHOT_ANALYTICS, "Analytics", PRIMARY, 1.84, 4.35, 1.65, 2.2),
        (SHOT_COACH, "Coach", ACCENT, 3.76, 4.35, 1.65, 2.2),
        (SHOT_PROFILE, "Profile", PRIMARY_SOFT, 5.68, 4.35, 1.65, 2.2),
    ]
    for image_path, title, accent, x, y, w, h in gallery:
        add_screenshot_card(slide, image_path, x, y, w, h, title, "", accent)
    slide.text(emu(8.0), emu(4.7), emu(4.0), emu(1.0), "Keep this slide for backup and Q&A support.", color=TEXT, size=13, bold=True)
    slide.text(emu(8.0), emu(5.55), emu(4.1), emu(0.7), "It should not take presentation time unless examiners request it.", color=MUTED, size=11.2)
    return slide


def theme_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="{NS_A}" name="DreamMind Theme">
  <a:themeElements>
    <a:clrScheme name="DreamMind">
      <a:dk1><a:srgbClr val="000000"/></a:dk1>
      <a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
      <a:dk2><a:srgbClr val="{BG}"/></a:dk2>
      <a:lt2><a:srgbClr val="{TEXT}"/></a:lt2>
      <a:accent1><a:srgbClr val="{PRIMARY}"/></a:accent1>
      <a:accent2><a:srgbClr val="{ACCENT}"/></a:accent2>
      <a:accent3><a:srgbClr val="{PRIMARY_SOFT}"/></a:accent3>
      <a:accent4><a:srgbClr val="{PINK}"/></a:accent4>
      <a:accent5><a:srgbClr val="{MUTED}"/></a:accent5>
      <a:accent6><a:srgbClr val="{CARD_ALT}"/></a:accent6>
      <a:hlink><a:srgbClr val="{PRIMARY}"/></a:hlink>
      <a:folHlink><a:srgbClr val="{PRIMARY_SOFT}"/></a:folHlink>
    </a:clrScheme>
    <a:fontScheme name="DreamMind Fonts">
      <a:majorFont><a:latin typeface="Aptos Display"/><a:ea typeface=""/><a:cs typeface=""/></a:majorFont>
      <a:minorFont><a:latin typeface="Aptos"/><a:ea typeface=""/><a:cs typeface=""/></a:minorFont>
    </a:fontScheme>
    <a:fmtScheme name="DreamMind Format">
      <a:fillStyleLst>
        <a:solidFill><a:schemeClr val="phClr"/></a:solidFill>
        <a:gradFill rotWithShape="1">
          <a:gsLst>
            <a:gs pos="0"><a:schemeClr val="phClr"><a:lumMod val="110000"/><a:satMod val="105000"/></a:schemeClr></a:gs>
            <a:gs pos="100000"><a:schemeClr val="phClr"><a:lumMod val="90000"/><a:satMod val="130000"/></a:schemeClr></a:gs>
          </a:gsLst>
          <a:lin ang="16200000" scaled="1"/>
        </a:gradFill>
        <a:gradFill rotWithShape="1">
          <a:gsLst>
            <a:gs pos="0"><a:schemeClr val="phClr"><a:alpha val="100000"/></a:schemeClr></a:gs>
            <a:gs pos="100000"><a:schemeClr val="phClr"><a:alpha val="0"/></a:schemeClr></a:gs>
          </a:gsLst>
          <a:path path="circle"><a:fillToRect l="50000" t="50000" r="50000" b="50000"/></a:path>
        </a:gradFill>
      </a:fillStyleLst>
      <a:lnStyleLst>
        <a:ln w="9525" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:prstDash val="solid"/></a:ln>
        <a:ln w="25400" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:prstDash val="solid"/></a:ln>
        <a:ln w="38100" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:prstDash val="solid"/></a:ln>
      </a:lnStyleLst>
      <a:effectStyleLst>
        <a:effectStyle><a:effectLst/></a:effectStyle>
        <a:effectStyle><a:effectLst/></a:effectStyle>
        <a:effectStyle><a:effectLst/></a:effectStyle>
      </a:effectStyleLst>
      <a:bgFillStyleLst>
        <a:solidFill><a:schemeClr val="phClr"/></a:solidFill>
        <a:solidFill><a:schemeClr val="phClr"><a:alpha val="50000"/></a:schemeClr></a:solidFill>
        <a:gradFill rotWithShape="1">
          <a:gsLst>
            <a:gs pos="0"><a:schemeClr val="phClr"><a:alpha val="100000"/></a:schemeClr></a:gs>
            <a:gs pos="100000"><a:schemeClr val="phClr"><a:alpha val="0"/></a:schemeClr></a:gs>
          </a:gsLst>
          <a:path path="circle"><a:fillToRect l="50000" t="50000" r="50000" b="50000"/></a:path>
        </a:gradFill>
      </a:bgFillStyleLst>
    </a:fmtScheme>
  </a:themeElements>
  <a:objectDefaults/>
  <a:extraClrSchemeLst/>
</a:theme>
"""


def slide_master_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">
  <p:cSld name="DreamMind Master">
    <p:bg><p:bgPr>{solid_fill(BG)}<a:effectLst/></p:bgPr></p:bg>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
  <p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst>
  <p:txStyles>
    <p:titleStyle><a:lvl1pPr algn="l"/></p:titleStyle>
    <p:bodyStyle><a:lvl1pPr marL="0" indent="0"/></p:bodyStyle>
    <p:otherStyle><a:lvl1pPr marL="0" indent="0"/></p:otherStyle>
  </p:txStyles>
</p:sldMaster>
"""


def slide_master_rels_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>
"""


def slide_layout_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}" type="blank" preserve="1">
  <p:cSld name="Blank">
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>
"""


def slide_layout_rels_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>
"""


def presentation_xml(slide_count: int) -> str:
    slide_ids = []
    for idx in range(slide_count):
        slide_ids.append(f'<p:sldId id="{256 + idx}" r:id="rId{idx + 2}"/>')
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">
  <p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst>
  <p:sldIdLst>{''.join(slide_ids)}</p:sldIdLst>
  <p:sldSz cx="{SLIDE_W}" cy="{SLIDE_H}"/>
  <p:notesSz cx="6858000" cy="9144000"/>
  <p:defaultTextStyle/>
</p:presentation>
"""


def presentation_rels_xml(slide_count: int) -> str:
    rels = [
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>',
        '<Relationship Id="rId999" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/presProps" Target="presProps.xml"/>',
    ]
    for idx in range(slide_count):
        rels.append(
            f'<Relationship Id="rId{idx + 2}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{idx + 1}.xml"/>'
        )
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        + "".join(rels)
        + "</Relationships>"
    )


def slide_rels_xml(slide: Slide, media_map: dict[Path, str]) -> str:
    rels = [
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>'
    ]
    for rel_id, path in slide.image_rels:
        rels.append(
            f'<Relationship Id="{rel_id}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/{media_map[path]}"/>'
        )
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        + "".join(rels)
        + "</Relationships>"
    )


def root_rels_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
"""


def content_types_xml(slide_count: int) -> str:
    overrides = [
        '<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>',
        '<Override PartName="/ppt/presProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"/>',
        '<Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>',
        '<Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>',
        '<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>',
        '<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>',
        '<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>',
    ]
    for idx in range(slide_count):
        overrides.append(
            f'<Override PartName="/ppt/slides/slide{idx + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>'
        )
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
        "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
        "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
        "<Default Extension=\"png\" ContentType=\"image/png\"/>"
        "<Default Extension=\"jpg\" ContentType=\"image/jpeg\"/>"
        "<Default Extension=\"jpeg\" ContentType=\"image/jpeg\"/>"
        + "".join(overrides)
        + "</Types>"
    )


def pres_props_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentationPr xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}"/>
"""


def core_xml() -> str:
    now = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>DreamMind Final Exam Presentation</dc:title>
  <dc:creator>OpenAI Codex</dc:creator>
  <cp:lastModifiedBy>OpenAI Codex</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">{now}</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">{now}</dcterms:modified>
</cp:coreProperties>
"""


def app_xml(slide_count: int) -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office PowerPoint</Application>
  <PresentationFormat>Custom</PresentationFormat>
  <Slides>{slide_count}</Slides>
  <Notes>0</Notes>
  <HiddenSlides>0</HiddenSlides>
  <MMClips>0</MMClips>
  <ScaleCrop>false</ScaleCrop>
  <Company>DreamMind</Company>
  <LinksUpToDate>false</LinksUpToDate>
  <SharedDoc>false</SharedDoc>
  <HyperlinksChanged>false</HyperlinksChanged>
  <AppVersion>16.0000</AppVersion>
</Properties>
"""


def build_presentation() -> dict[str, bytes]:
    slides = [
        make_slide_1(),
        make_slide_2(),
        make_slide_3(),
        make_slide_4(),
        make_slide_5(),
        make_slide_6(),
        make_slide_7(),
        make_slide_8(),
        make_slide_9(),
        make_slide_10(),
        make_slide_11(),
        make_slide_12(),
        make_slide_13(),
    ]
    slide_count = len(slides)

    unique_images: list[Path] = []
    seen_images: set[Path] = set()
    for slide in slides:
        for _, image_path in slide.image_rels:
            if image_path not in seen_images:
                unique_images.append(image_path)
                seen_images.add(image_path)

    media_map = {
        image_path: f"image{index + 1}{image_path.suffix.lower()}"
        for index, image_path in enumerate(unique_images)
    }

    files: dict[str, bytes] = {
        "[Content_Types].xml": content_types_xml(slide_count).encode("utf-8"),
        "_rels/.rels": root_rels_xml().encode("utf-8"),
        "docProps/core.xml": core_xml().encode("utf-8"),
        "docProps/app.xml": app_xml(slide_count).encode("utf-8"),
        "ppt/presentation.xml": presentation_xml(slide_count).encode("utf-8"),
        "ppt/_rels/presentation.xml.rels": presentation_rels_xml(slide_count).encode("utf-8"),
        "ppt/presProps.xml": pres_props_xml().encode("utf-8"),
        "ppt/theme/theme1.xml": theme_xml().encode("utf-8"),
        "ppt/slideMasters/slideMaster1.xml": slide_master_xml().encode("utf-8"),
        "ppt/slideMasters/_rels/slideMaster1.xml.rels": slide_master_rels_xml().encode("utf-8"),
        "ppt/slideLayouts/slideLayout1.xml": slide_layout_xml().encode("utf-8"),
        "ppt/slideLayouts/_rels/slideLayout1.xml.rels": slide_layout_rels_xml().encode("utf-8"),
    }
    for idx, slide in enumerate(slides, start=1):
        files[f"ppt/slides/slide{idx}.xml"] = slide.build_xml().encode("utf-8")
        files[f"ppt/slides/_rels/slide{idx}.xml.rels"] = slide_rels_xml(slide, media_map).encode("utf-8")
    for image_path, media_name in media_map.items():
        files[f"ppt/media/{media_name}"] = image_path.read_bytes()
    return files


def validate_xml_strings(files: dict[str, bytes]) -> None:
    import xml.etree.ElementTree as ET

    for name, xml in files.items():
        if name.endswith(".xml") or name.endswith(".rels"):
            ET.fromstring(xml.decode("utf-8"))


def write_pptx(target: Path) -> None:
    files = build_presentation()
    validate_xml_strings(files)
    target.parent.mkdir(parents=True, exist_ok=True)
    with ZipFile(target, "w", compression=ZIP_DEFLATED) as zf:
        for name, payload in files.items():
            zf.writestr(name, payload)


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    output = root / "DreamMind_Final_Exam_Presentation.pptx"
    write_pptx(output)
    print(output)


if __name__ == "__main__":
    main()
