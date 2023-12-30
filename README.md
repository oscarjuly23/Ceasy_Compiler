# Ceasy Compiler
Ceasy Compiler es un proyecto que surge en el contexto de una práctica académica, con el objetivo de diseñar un lenguaje de programación propio y construir un compilador que lo traduzca a código de máquina. Este proceso se realiza en dos etapas principales: la definición del lenguaje y la implementación del compilador.  

El proceso se divide en dos partes principales: la definición y diseño del lenguaje y la implementación del compilador. Utilizamos Java para el desarrollo del compilador, centrándonos en el front-end y adaptándolo para el back-end con la arquitectura MIPS.   

Adoptamos una metodología Agile, utilizando herramientas como Jira y Bitbucket para la planificación y organización del trabajo en equipo.

## Módulos del Compilador
### Estructura General
El compilador Ceasy consta de varios módulos esenciales que cubren desde el preprocesador hasta la generación del código MIPS, cada uno desempeñando una función crítica en la transformación del código fuente.

### Preprocesador
Encargado de limpiar y simplificar el código antes de la compilación, el preprocesador realiza tareas como eliminar comentarios y expandir definiciones (defines), mejorando la legibilidad y facilitando la compilación.

### Análisis Léxico
Genera el "String of Tokens" a partir del código preprocesado, identificando tokens válidos y generando un archivo con información relevante. Detecta y maneja errores para garantizar la integridad del análisis léxico.

### Análisis Sintáctico
Construye el árbol de análisis sintáctico según la gramática definida, detectando errores en la estructura del código. Almacena variables en la tabla de símbolos y gestiona su ámbito.

### Análisis Semántico
Valida declaraciones semánticamente, asegurando la coherencia de tipos y el uso adecuado de variables. Recorre el árbol generado por el análisis sintáctico para realizar estas comprobaciones.

### Generador de Código Intermedio
Crea el "Three Address Code (TAC)" como una lista de cuádruplas, generando información basada en el árbol de análisis sintáctico.

### Optimizaciones
Aplica optimizaciones en el backend, adaptándose a la arquitectura MIPS32. Incluye asignación eficiente de registros, inicialización directa de variables y simplificación de operaciones con constantes.

### MIPS
Convierte cuádruplas a código MIPS32, traduciendo cada operación a su equivalente en MIPS32 y volcando el código en un archivo para su ejecución.

## Información del Lenguaje

### Motivación
Ceasy surge de la experiencia en diversos lenguajes de programación y la oportunidad de crear uno propio. Inspirado en 'C', busca ofrecer funcionalidades que otros lenguajes no permiten. La meta es crear un lenguaje fácil de entender y aprender, especialmente para aquellos que se inician en la programación.

### Datos Básicos
- **Nombre:** Ceasy
  - Juego de palabras entre 'C' y 'easy' (fácil en inglés), reflejando la naturaleza accesible del lenguaje.
- **Extensión de Archivo:** .cy
- **Tipo de Lenguaje:** Compilado
  - La compilación multi-pasada permite un control de errores más temprano y facilita el debugging.
- **Paradigma:** Imperativo
- **Tipado:** Débil
  - El tipado débil se elige para simplificar el lenguaje y hacerlo más accesible.
- **Domino:** Genérico
  - No se enfoca en un dominio específico, siendo adecuado para aprender programación en general.

## [Gramática](https://github.com/oscarjuly23/Ceasy_Compiler/blob/main/LLPR2122_Memoria_G1.pdf) 
Ya que  la gramática de este lenguaje de programación es bastante extensa he decidido adjuntar el PDF dónde se explica a detalle toda la gramática que sigue.

## Metodología de Trabajo

**Metodología:** Agile  
**Herramientas:** Jira para sprints, Bitbucket para control de versiones.

**Desarrollo:**
- Trabajamos en sprints semanales asignando tareas individuales.
- Utilizamos ramas (master, dev) para versiones estables e inacabadas.
- Reuniones semanales para coordinación y asignación de tareas.
- Pair programming para tareas complejas.

**Comunicación:**
- Grupos de WhatsApp, Discord y llamadas para decisiones rápidas.

**Aprendizajes:**
- Valor de reuniones en metodología Agile.
- Efectividad de herramientas como Jira y Bitbucket.

## Conclusiones

**Aprendizaje Incremental:**
- Aplicación práctica de la teoría vista en la asignatura.
- Aprendizaje progresivo de conceptos teóricos.

**Metodología Agile:**
- Utilización efectiva de sprints, stories y tasks.
- Organización progresiva del trabajo en un proyecto de gran escala.
- Trabajo en equipo y responsabilidad individual.

**Comunicación y Organización:**
- División del trabajo y reuniones para avanzar partes específicas.
- Colaboración en el diseño y comprensión del código.

**Dificultades Superadas:**
- Comprensión profunda del funcionamiento de los compiladores.
- Importancia de escribir un buen código y usar el lenguaje adecuado.

En resumen, la práctica ha sido un desafío en el que hemos aplicado y comprendido aspectos teóricos de manera práctica, hemos trabajado de manera organizada con metodologías ágiles, y hemos fortalecido nuestras habilidades individuales y de equipo.


##

- @date 20 de Mayo 2022
- @author Oscar Julian (oscar.julian)
- @author Carles Torrubiano (carles.torrubiamo)
- @author Victor Vallés (v.valles)
- @author Bernat Segura (bernat.segura)
- @author Rafael Morera (rafael.morera)

### Execució
- Executar /src/main.java
- El codi del arxiu 'file.cy' serà el que s'executi en el programa.
- Els arxius generat per el programa es troben a la carpeta 'Output files'
- 

### Edició de FLAGS
Pestanya 'Run' -->
Opció 'Edit Configurations...' -->
Apartat 'Build and run' -->
Paràmetre 'Program arguments' --> -- FLAG

##### Flags
-- all
-- preprocessor
-- lexical
-- syntactic
-- semantic
