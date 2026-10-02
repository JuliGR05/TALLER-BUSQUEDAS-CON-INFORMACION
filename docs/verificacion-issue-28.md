# Verificación issue #28 — Taller Word vs informe borrado vs código

Fecha: 2026-10-02. Rama: `docs/completar-informe-issue-28`.
Fuentes comparadas:
- Taller Word: `/home/johan/Descargas/TALLER BÚSQUEDAS CON INFORMACIÓN.docx.md` (250 líneas, con figuras base64).
- Informe borrado: commit `a036688` (`docs/informe-final.md`, 231 líneas, restaurado en esta rama).
- Código: paquete `conmutacion` en `develop` (compila con `javac -encoding UTF-8 -d out conmutacion/*.java`).
- Issue #28: `[P3] Informe final: unir secciones, revisión cruzada y entrega`.

## 1. Resultado de la ejecución real (verificado hoy)

```
Nodos: R1,R2,R3,R4 (NUCLEO), A,B,C (BORDE)
R1-R2 6.20 | R1-R3 7.20 | R2-R3 5.20 | R2-R4 7.20 | R3-R4 6.20
A-R1 32.00 | A-R2 36.50 | B-R3 32.00 | B-R4 36.50 | C-R2 32.00 | C-R4 36.50

A (5 paq, sin eventos): 5x A -> R1 -> R3 -> B (71.2, 72.2, 73.2, 74.2, 75.2)
B (M1 4 paq A->B + M2 3 paq C->B, B-R3 -> congestion 0.5 tras paq 2):
  M1-P1 71.2 A->R1->R3->B | M1-P2 72.2 A->R1->R3->B
  M2-P1 77.0 C->R4->B | M2-P2 78.0 | M2-P3 79.0
  M1-P3 82.2 A->R2->R4->B | M1-P4 83.2 A->R2->R4->B
C (5 paq A->B, cae A-R1 tras paq 2):
  M1-P1 71.2, M1-P2 72.2 por A->R1->R3->B
  M1-P3 75.7, M1-P4 76.7, M1-P5 77.7 por A->R2->R3->B (73.7)
```

Los tres escenarios coinciden al decimal con la Tabla 2 y §§6–7 del Word y con el §3 del informe borrado.

## 2. Comparativa (¿falta algo?)

| Aspecto (issue #28) | Informe borrado `a036688` | Taller Word (docx) | Código | Estado |
|---|---|---|---|---|
| Punto 1 Rumania (13 iters, D/P, 418 km, Fagaras 468 vs 418) | Sí (§1 + `punto1-dijkstra-rumania.md`) | **No**: sin mención a Rumania/Arad/Bucharest; §1 vacío | N/A (solo informe) | **Falta en Word** |
| Problema: entradas/salidas/condiciones/fórmula | Sí (§2.1–2.2) | Sí (§2, más tabla de términos y ejemplo 6.5/34/44) | `CalculadoraPeso` (2.0/100/100), `Enlace`, `Red` OK | OK ambos, Word más didáctico |
| Algoritmo + pseudocódigo + complejidad O((V+E) log V) | Sí (§2.3 + `punto2-algoritmo-simulacion.md`) | **No**: describe recálculo por paquete pero sin pseudocódigo ni complejidad | `Simulador` (envío `(i-1)*1.0`, evento tras paq `i`, orden por llegada) + `Dijkstra` OK | **Falta en Word** |
| Red: Tabla 1 nodos + Tabla 2 enlaces/pesos | Sí (§2.4) | Sí (§4 + Tablas 1–2, coinciden: 6.2/7.2/5.2 y 32.0/36.5) | `Main.construirRed` OK | OK, coinciden |
| Diagramas (clases §3, red §4, capturas A/B/C §§7) | No (solo texto) | Sí (image2–image6) | N/A | **Falta en informe .md** (se referencia aquí) |
| Resultados A/B/C con rutas, costos y llegadas | Sí (§3, texto consola) | Sí (§§6–8, tabla + capturas, mismos números) | Salida `Main`+`SalidaConsola` OK | OK, coinciden |
| Revisión cruzada + verificación 2 puntos | Sí (§4 + Referencias, issues #15–#28, Cormen) | No (solo conclusiones §9) | Reglas issue #15 respetadas | **Falta en Word** |
| Numeración/entrega PDF | Informe .md → PDF (en `a036688`) | Numeración rota: §1 vacío, salta de §4 a §6 (sin §5); ejemplo salida §136 aún con placeholder 25.5/27.0 | N/A | **A corregir en Word** |

## 3. Conclusión

- El código está completo y verificado: fórmula, red, eventos, Dijkstra por paquete y orden de llegada cumplen el diseño del issue #15 y los números del taller.
- El Word cubre bien el Punto 2 (problema, red, escenarios, conclusiones, figuras) pero **no cubre el Punto 1 ni el pseudocódigo/complejidad ni las referencias**, que sí exige el issue #28 ("unir en orden Punto 1, Punto 2… con portada y referencias" y "verificar los dos puntos").
- El informe `.md` restaurado sí cubre Punto 1 + Punto 2 + revisión + referencias. Lo que le faltaba eran las figuras del Word.

## 4. Qué hace esta rama (cierre #28)

- [x] Restaura `docs/informe-final.md` desde `a036688` (Punto 1, Punto 2, resultados reales, revisión, referencias).
- [x] Añade §5 en `informe-final.md` con el checklist del issue #28 y el enlace a este archivo.
- [x] Deja constancia de que las figuras (diagrama de clases, red, capturas A/B/C) están en la versión Word; no se duplican base64 en el repo.
- [ ] Pendiente fuera del repo: en el Word, añadir §1 Punto 1 (resumen 418 km + link a `punto1-*.md`), §5 pseudocódigo/complejidad (resumen + link a `punto2-*.md`), corregir numeración (§1/§5), reemplazar ejemplo placeholder 25.5 por salida real 71.2, y exportar PDF.
- [x] Verificado: `javac` + `java conmutacion.Main` reproduce los números citados.
