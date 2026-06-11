# Mathematical Specification: Atmospheric Background (Reddish Tone)

This document provides the rigorous mathematical specification for the background layer implemented with the reddish tone update.

---

## 1. Design Token Inputs

*   **Dimensions:** $360\text{px} \times 326\text{px}$
*   **Angle:** $180^\circ$ (Top-to-Bottom)
*   **Color Stop 0% (Top):** Solid White (`#FFFFFF`, $1.0$ Alpha)
*   **Color Stop 100% (Bottom):** Semi-transparent Reddish Tone (`rgba(197, 64, 80, 0.30)`)
*   **Post-Processing:** $52\text{px}$ Spatial Gaussian Blur simulation.

---

## 2. Mathematical Vector Definitions

### 2.1 Color Normalization
**Top Color ($C_{\text{top}}$):**
$$C_{\text{top}} = \begin{pmatrix} 1.0 \\ 1.0 \\ 1.0 \\ 1.0 \end{pmatrix}$$

**Bottom Color ($C_{\text{bottom\_raw}}$):**
$R = 197/255 \approx 0.77255$
$G = 64/255 \approx 0.25098$
$B = 80/255 \approx 0.31372$
$A = 0.3$

### 2.2 Pre-multiplied Alpha (AGSL Pipeline Requirement)
To prevent alpha-bleeding artifacts in the Android pipeline:
$$C_{\text{bottom}} = \begin{pmatrix} R \cdot A \\ G \cdot A \\ B \cdot A \\ A \end{pmatrix} = \begin{pmatrix} 0.77255 \cdot 0.3 \\ 0.25098 \cdot 0.3 \\ 0.31372 \cdot 0.3 \\ 0.3 \end{pmatrix} \approx \begin{pmatrix} 0.23177 \\ 0.07529 \\ 0.09412 \\ 0.3 \end{pmatrix}$$

---

## 3. Implementation Logic

### 3.1 Gradient Curve
The mix is controlled by a smoothstep on the vertical coordinate $v$:
$$t = \text{smoothstep}(-0.05, 0.95, v)$$
$$C_{\text{mixed}} = \text{mix}(C_{\text{top}}, C_{\text{bottom}}, t)$$

### 3.2 52px Blur Simulation (Feather Masking)
The "cloud-like" edge bleed is achieved via 4-way precision feathering:
*   $m_{left} = \text{smoothstep}(0.0, 0.08, u)$
*   $m_{right} = \text{smoothstep}(1.0, 0.92, u)$
*   $m_{top} = \text{smoothstep}(0.0, 0.04, v)$
*   $m_{bottom} = \text{smoothstep}(1.0, 0.92, v)$

Final Color:
$$C_{final} = C_{mixed} \cdot (m_{left} \cdot m_{right} \cdot m_{top} \cdot m_{bottom})$$
