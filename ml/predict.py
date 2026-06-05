from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

import torch
from PIL import Image

ML_ROOT = Path(__file__).resolve().parent
if str(ML_ROOT) not in sys.path:
    sys.path.insert(0, str(ML_ROOT))

from sample_detection.cnn import build_model
from sample_detection.preprocessing import crop_bbox, preprocess_image

DEFAULT_CHECKPOINT = ML_ROOT / "checkpoints" / "sample_detector_resnet18.pt"
DISPLAY_LABELS_BY_INDEX = {
    0: "образец с нарушенной структурой",
    1: "качественный образец",
    2: "стандартный образец",
    3: "образец с примесями",
    4: "нецелевой образец",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Detect the sample class from an image.")
    parser.add_argument("--image", required=True, help="Image to analyze.")
    parser.add_argument("--checkpoint", default=str(DEFAULT_CHECKPOINT), help="Path to .pt checkpoint.")
    parser.add_argument(
        "--bbox",
        nargs=4,
        type=float,
        metavar=("X", "Y", "W", "H"),
        help="Optional crop rectangle: x y width height.",
    )
    parser.add_argument("--top-k", type=int, default=5)
    parser.add_argument("--format", choices=("json", "properties"), default="json")
    return parser.parse_args()


@torch.no_grad()
def predict_image(
    checkpoint_path: str | Path,
    image_path: str | Path,
    bbox: list[float] | tuple[float, float, float, float] | None = None,
    top_k: int = 5,
) -> dict:
    checkpoint_path = Path(checkpoint_path)
    image_path = Path(image_path)

    checkpoint = torch.load(checkpoint_path, map_location="cpu")
    class_to_idx = checkpoint["class_to_idx"]
    idx_to_class = {int(idx): label for label, idx in class_to_idx.items()}

    model = build_model(
        num_classes=len(class_to_idx),
        dropout=float(checkpoint.get("dropout", 0.25)),
        model_name=checkpoint.get("model_name", "simple_cnn"),
        pretrained=False,
        freeze_backbone=bool(checkpoint.get("freeze_backbone", False)),
    )
    model.load_state_dict(checkpoint["model_state_dict"])
    model.eval()

    image = Image.open(image_path).convert("RGB")
    if bbox is not None:
        image = crop_bbox(image, bbox)

    tensor = preprocess_image(image, int(checkpoint.get("image_size", 224))).unsqueeze(0)
    probabilities = torch.softmax(model(tensor), dim=1).squeeze(0)
    best_idx = int(torch.argmax(probabilities).item())

    ordered = []
    k = min(top_k, len(idx_to_class))
    scores, indices = torch.topk(probabilities, k=k)
    for score, index in zip(scores, indices):
        label_index = int(index)
        ordered.append(
            {
                "label": display_label(label_index),
                "probability": round(float(score), 6),
            }
        )

    return {
        "image_path": str(image_path),
        "checkpoint_path": str(checkpoint_path),
        "model_name": checkpoint.get("model_name", "image_detector"),
        "predicted_class": display_label(best_idx),
        "confidence": round(float(probabilities[best_idx]), 6),
        "probabilities": ordered,
    }


def display_label(class_index: int) -> str:
    return DISPLAY_LABELS_BY_INDEX.get(class_index, f"класс {class_index}")


def print_properties(result: dict) -> None:
    print(f"predicted_class={result['predicted_class']}")
    print(f"confidence={result['confidence']}")
    print(f"model_name={result['model_name']}")
    print(f"probability.count={len(result['probabilities'])}")
    for index, item in enumerate(result["probabilities"]):
        print(f"probability.{index}.label={item['label']}")
        print(f"probability.{index}.value={item['probability']}")


def main() -> None:
    args = parse_args()
    result = predict_image(
        checkpoint_path=args.checkpoint,
        image_path=args.image,
        bbox=args.bbox,
        top_k=args.top_k,
    )
    if args.format == "properties":
        print_properties(result)
    else:
        print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()

