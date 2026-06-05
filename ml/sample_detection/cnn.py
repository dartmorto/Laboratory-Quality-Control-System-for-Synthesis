from __future__ import annotations

import torch
from torch import nn
from torchvision import models


SIMPLE_CNN = "simple_cnn"
RESNET18 = "resnet18"
EFFICIENTNET_B0 = "efficientnet_b0"
MODEL_NAMES = (SIMPLE_CNN, RESNET18, EFFICIENTNET_B0)


class SimpleCNN(nn.Module):
    """Small convolutional baseline for image or crop classification."""

    def __init__(self, num_classes: int, dropout: float = 0.25) -> None:
        super().__init__()
        if num_classes < 2:
            raise ValueError("num_classes must be at least 2")

        self.features = nn.Sequential(
            self._block(3, 32),
            self._block(32, 64),
            self._block(64, 128),
            self._block(128, 256),
            nn.AdaptiveAvgPool2d((1, 1)),
        )
        self.classifier = nn.Sequential(
            nn.Flatten(),
            nn.Dropout(dropout),
            nn.Linear(256, 128),
            nn.ReLU(inplace=True),
            nn.Dropout(dropout),
            nn.Linear(128, num_classes),
        )

    @staticmethod
    def _block(in_channels: int, out_channels: int) -> nn.Sequential:
        return nn.Sequential(
            nn.Conv2d(in_channels, out_channels, kernel_size=3, padding=1, bias=False),
            nn.BatchNorm2d(out_channels),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(kernel_size=2),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.features(x)
        return self.classifier(x)


def build_model(
    num_classes: int,
    dropout: float = 0.25,
    model_name: str = SIMPLE_CNN,
    pretrained: bool = False,
    freeze_backbone: bool = False,
) -> nn.Module:
    if model_name == SIMPLE_CNN:
        return SimpleCNN(num_classes=num_classes, dropout=dropout)
    if model_name == RESNET18:
        return build_resnet18(num_classes, dropout, pretrained, freeze_backbone)
    if model_name == EFFICIENTNET_B0:
        return build_efficientnet_b0(num_classes, dropout, pretrained, freeze_backbone)
    raise ValueError(f"Unknown model_name={model_name!r}; expected one of {MODEL_NAMES}")


def build_resnet18(
    num_classes: int,
    dropout: float,
    pretrained: bool,
    freeze_backbone: bool,
) -> nn.Module:
    weights = models.ResNet18_Weights.DEFAULT if pretrained else None
    model = models.resnet18(weights=weights)
    if freeze_backbone:
        for parameter in model.parameters():
            parameter.requires_grad = False

    in_features = model.fc.in_features
    model.fc = nn.Sequential(
        nn.Dropout(dropout),
        nn.Linear(in_features, num_classes),
    )
    return model


def build_efficientnet_b0(
    num_classes: int,
    dropout: float,
    pretrained: bool,
    freeze_backbone: bool,
) -> nn.Module:
    weights = models.EfficientNet_B0_Weights.DEFAULT if pretrained else None
    model = models.efficientnet_b0(weights=weights)
    if freeze_backbone:
        for parameter in model.parameters():
            parameter.requires_grad = False

    in_features = model.classifier[1].in_features
    model.classifier = nn.Sequential(
        nn.Dropout(dropout),
        nn.Linear(in_features, num_classes),
    )
    return model
