from __future__ import annotations

from PIL import Image
from torchvision import transforms

IMAGENET_MEAN = (0.485, 0.456, 0.406)
IMAGENET_STD = (0.229, 0.224, 0.225)
DEFAULT_IMAGE_SIZE = 224


def crop_bbox(
    image: Image.Image,
    bbox: list[float] | tuple[float, float, float, float],
) -> Image.Image:
    width, height = image.size
    x, y, bbox_width, bbox_height = (float(value) for value in bbox)

    left = max(0, min(width - 1, int(round(x))))
    top = max(0, min(height - 1, int(round(y))))
    right = max(left + 1, min(width, int(round(x + bbox_width))))
    bottom = max(top + 1, min(height, int(round(y + bbox_height))))
    return image.crop((left, top, right, bottom))


def build_val_transforms(image_size: int = DEFAULT_IMAGE_SIZE) -> transforms.Compose:
    return transforms.Compose(
        [
            transforms.Resize((image_size, image_size)),
            transforms.ToTensor(),
            transforms.Normalize(mean=IMAGENET_MEAN, std=IMAGENET_STD),
        ]
    )


def preprocess_image(image: Image.Image, image_size: int = DEFAULT_IMAGE_SIZE):
    return build_val_transforms(image_size)(image)
