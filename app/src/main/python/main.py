# This file converts an image to a Colorblind friendly image

# imports
import argparse
import sys
import io

import numpy as np
from daltonlens import simulate
from PIL import Image
from skimage.color import lab2rgb, rgb2lab


class LabComponent:
    LUMIN = 0
    GREENTORED = 1
    YELLOWTOBLUE = 2


def make_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="Correcter", description="Corrects an colorblind-unfriendly image"
    )
    p.add_argument("image_path")
    p.add_argument("-t", "--type", type=str)
    p.add_argument("-s", "--severity", type=float)
    return p


def str_to_cb_type(str_as_Cb_type: str) -> simulate.Deficiency:
    if str_as_Cb_type in ["Deutaronopia", "D"]:
        return simulate.Deficiency.DEUTAN
    elif str_as_Cb_type in ["Protanopia", "P"]:
        return simulate.Deficiency.PROTAN
    elif str_as_Cb_type in ["Tritanopia", "T"]:
        return simulate.Deficiency.TRITAN
    else:
        return simulate.Deficiency.DEUTAN


def simulate_colorblindness(
        i: np.ndarray, cb_type: simulate.Deficiency, cb_severity: float
) -> np.ndarray:
    return simulate.Simulator_Vienot1999().simulate_cvd(
        i, cb_type, severity=cb_severity
    )


def normalize(data: np.ndarray) -> np.ndarray:
    normalized_gradient = (data / np.max(data) * 255).astype(np.uint8)
    return normalized_gradient


def get_lab_diff(
        data: np.ndarray, simulation: np.ndarray, diff_component
) -> np.ndarray:
    h, w = data.shape[:2]
    canvas_lumin = np.zeros((h, w))
    canvas_2 = np.zeros((h,w))
    canvas_3 = np.zeros((h,w))
    for i in range(h):
        for j in range(w):
            canvas_lumin[i, j] = simulation[i, j][LabComponent.LUMIN] - data[i, j][LabComponent.LUMIN]
            canvas_2[i, j] = simulation[i, j][LabComponent.GREENTORED] - data[i, j][LabComponent.GREENTORED]
            canvas_2[i,j] *= -1
            canvas_3[i, j] = simulation[i, j][LabComponent.YELLOWTOBLUE] - data[i, j][LabComponent.YELLOWTOBLUE]

    return canvas_lumin, canvas_2, canvas_3


def correct_image(
        original: np.ndarray,
        mask: np.ndarray,
        merged_masks: np.ndarray,
) -> np.ndarray:
    canvas = original.copy()
    canvas = rgb2lab(canvas)
    canvas[:, :, 2] -= merged_masks * 2.1
    canvas[:, :, 1] += merged_masks * 0.05
  #   canvas[:, :, 0] += mask * 5
    canvas = lab2rgb(canvas)
    return canvas


def corrected_sim(cb_type, severity, filepath):
    severity = float(severity)
    image = np.array(Image.open(filepath).convert("RGB"))
    simulation = normalize(
        simulate_colorblindness(image, str_to_cb_type(cb_type), severity)
    )

    image_lab = rgb2lab(image)
    simulation_lab = rgb2lab(simulation)

    lumin_diff, gtr_diff ,ytb_diff = get_lab_diff(image_lab, simulation_lab, LabComponent.YELLOWTOBLUE)

    lumin_diff[lumin_diff < 0] = 0
    merged_masks = gtr_diff + ytb_diff
    merged_masks[merged_masks < 0] = 0

    corrected_image = np.array(
        (correct_image(image, lumin_diff, merged_masks) * 255).astype(np.uint8)
    )
    sim_corrected = simulate_colorblindness(
        corrected_image, str_to_cb_type(cb_type),severity
    )
    byte_io = io.BytesIO()
    i = Image.fromarray(image)
    i.save(byte_io, format='PNG')
    byte_data = byte_io.getvalue()

    byte_io = io.BytesIO()

    i = Image.fromarray(simulation)
    i.save(byte_io, format='PNG')
    byte_data_image = byte_io.getvalue()

    byte_io = io.BytesIO()
    i = Image.fromarray(corrected_image)
    i.save(byte_io, format='PNG')
    byte_data_corrected = byte_io.getvalue()

    byte_io = io.BytesIO()
    i = Image.fromarray(sim_corrected)
    i.save(byte_io, format='PNG')
    corrected_simulation = byte_io.getvalue()


    return byte_data, byte_data_image, byte_data_corrected, corrected_simulation

# This numpy array has to be send over to java as a bmp file somehow
