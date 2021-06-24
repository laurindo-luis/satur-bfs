package br.com.ufma;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    //Vetor que representa cada vértice e sua cor atribuida
    static Integer[] vertexColor;
    static Integer[][] graphAdjacencyMatrix;
    static Integer numbersVertex;

    //Grau do Sudoku
    static Integer degreeSudoku;

    static Integer HEURISTIC_BREADTH_FIRST_SEARCH_SATURATED = 1;
    static Integer HEURISTIC_BREADTH_FIRST_SEARCH = 2;
    static Integer HEURISTIC_DEPTH_FIRST_SEARCH = 3;

    public static void main(String[] args) {
        evaluation();
    }

    public static void evaluation () {

        /* degree - grau do Sudoku
        *  blockLineNumbers e blockColumnNumbers representam a dimensão das submatrizes do Sudoku
         */
        List<SudokuType> sudokuTypes =  Arrays.asList(
                new SudokuType(4, 2,2),
                new SudokuType(6, 2,3),
                new SudokuType(8, 4,2),
                new SudokuType(9, 3,3),
                new SudokuType(10, 2,5),
                new SudokuType(12, 3,4)
        );

        List<SudokuLevels> levels = Arrays.asList(
                new SudokuLevels("easy", 15000),
                new SudokuLevels("medium", 30000),
                new SudokuLevels("difficult", 45000)
        );

        sudokuTypes.forEach(sudoku -> {
            System.out.println(String.format(
                    "---------------- \n" +
                    "Sudoku %sx%s \n" +
                    "----------------",
                    sudoku.getDegree(), sudoku.getDegree()));
            degreeSudoku = sudoku.getDegree();
            numbersVertex = degreeSudoku * degreeSudoku;
            graphAdjacencyMatrix = generateAdjacencyMatrix(degreeSudoku, sudoku.getBlockLineNumbers(),
                    sudoku.getBlockColumnNumbers());

            levels.forEach(level -> {
                System.out.println(String.format("> %s level".toUpperCase(), level.getLevel()));
                int maxIter = level.getMaxIter();
                String dirSudoku = String.format("sudokus/%sx%s/%s/sudoku%sx%s_%s",
                        sudoku.getDegree(), sudoku.getDegree(), level.getLevel(), sudoku.getDegree(),
                        sudoku.getDegree(), level.getLevel());

                System.out.println("##  SaturBFS  ##");
                executeTestMultiStart(HEURISTIC_BREADTH_FIRST_SEARCH_SATURATED, maxIter, dirSudoku);
                System.out.println("##  BFS  ##");
                executeTestMultiStart(HEURISTIC_BREADTH_FIRST_SEARCH, maxIter, dirSudoku);
                System.out.println("##  DFS  ##");
                executeTestMultiStart(HEURISTIC_DEPTH_FIRST_SEARCH, maxIter, dirSudoku);
            });
        });
    }

    public static void executeTestMultiStart (int optionHeuristic, int maxIteration, String sudokuFileDirectory) {
        int numberOfSudokuEachLevel = 10;
        int numberOfAttempts = 10;
        int numberOfHits = 0;
        int nextSudoku = 1;
        double mediumTime = 0;
        boolean isSolution;
        int contSolutionsOfSudokuEachLevel = 0;

        while (nextSudoku <= numberOfSudokuEachLevel) {
            int attempt = 1;
            isSolution = false;

            //Tempo ínicio
            double begin = Calendar.getInstance().getTimeInMillis();

            while (attempt <= numberOfAttempts) {
                boolean status = false;

                if(optionHeuristic == HEURISTIC_BREADTH_FIRST_SEARCH_SATURATED) {
                    status = multiStartBreadthFirstSearchSaturated (
                            String.format("%s_%s", sudokuFileDirectory, nextSudoku),
                            maxIteration
                    );
                } else if(optionHeuristic == HEURISTIC_BREADTH_FIRST_SEARCH) {
                    status = multiStartBreadthFirstSearch (
                            String.format("%s_%s", sudokuFileDirectory, nextSudoku),
                            maxIteration
                    );
                } else if(optionHeuristic == HEURISTIC_DEPTH_FIRST_SEARCH) {
                    status = multiStartDepthFirstSearch(
                            String.format("%s_%s", sudokuFileDirectory, nextSudoku),
                            maxIteration
                    );
                }

                if(status) {
                    numberOfHits++;
                    double end = 0;
                    if(isSolution == false) {
                        end = Calendar.getInstance().getTimeInMillis();
                        mediumTime += ((end - begin) / 1000);
                    }
                    isSolution = true;
                }
                attempt++;
            }
            if (isSolution) {
                contSolutionsOfSudokuEachLevel++;
            }
            nextSudoku++;
        }

        mediumTime /= contSolutionsOfSudokuEachLevel;
        double percentagem = (numberOfHits * 100) / (double)(numberOfSudokuEachLevel * numberOfAttempts);

        System.out.println(String.format("|  Tempo médio: %.4f segundos", mediumTime));
        System.out.println(String.format("|  Percentagem de soluções válidas: %.2f", percentagem)+"%");
    }

    public static boolean multiStartBreadthFirstSearchSaturated(String sudokuFileDirectory, int maxIter) {
        int numberAttemptsMultiStart = 1;

        while (numberAttemptsMultiStart <= maxIter) {
            readSudokuFile(sudokuFileDirectory);
            Boolean status = saturBFS();
            if(status)
                return true;
            numberAttemptsMultiStart++;
        }
        return false;
    }

    public static boolean multiStartBreadthFirstSearch (String sudokuFileDirectory, int maxIter) {
        int numberAttemptsMultiStart = 1;

        while (numberAttemptsMultiStart <= maxIter) {
            readSudokuFile(sudokuFileDirectory);
            Boolean status = BFS();
            if(status)
                return true;
            numberAttemptsMultiStart++;
        }
        return false;
    }

    public static boolean multiStartDepthFirstSearch (String sudokuFileDirectory, int maxIter) {
        int numberAttemptsMultiStart = 1;

        while (numberAttemptsMultiStart <= maxIter) {
            readSudokuFile(sudokuFileDirectory);
            Boolean status = DFS();
            if(status)
                return true;
            numberAttemptsMultiStart++;
        }
        return false;
    }

    public static Integer numbersColoredVertex() {
        Integer cont = 0;
        for (Integer corVertex : vertexColor) {
            if(corVertex != null)
                cont++;
        }
        return cont;
    }

    public static void readSudokuFile(String nomeArquivo) {
        int cont = 0;
        vertexColor = new Integer[numbersVertex];

        try {
            FileReader file = new FileReader(nomeArquivo+".txt");
            BufferedReader bufferedReader = new BufferedReader(file);

            String line = bufferedReader.readLine();
            String[] values;

            while ((line != null) && !(line.equals(""))) {
                values = line.split("-");

                for (int i = 0; i < values.length; i++) {
                    Integer value = Integer.valueOf(values[i].trim());
                    if(value != 0) {
                        vertexColor[cont] = value;
                    }
                    cont++;
                }
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //SaturBFS - Algoritmo Proposto - Busca largura usando Saturação para todos os vértices
    public static Boolean saturBFS() {
        Integer verticeInicio = highestSaturationVertex();
        Integer cor = colorirVertice(verticeInicio, degreeSudoku);
        vertexColor[verticeInicio] = cor;

        List<Integer> fila = new ArrayList<>();
        fila.add(verticeInicio);

        while (!fila.isEmpty()) {
            Integer vertice = fila.get(0);
            fila.remove(0);

            Integer verticeAdj = highestSaturationAdjacent(findAdjacentyVertex(vertice));

            while (verticeAdj != null) {
                cor = colorirVertice(verticeAdj, degreeSudoku);
                if(cor == null) {
                    //System.out.println("Não achou uma cor para o vértice "+verticeAdj);
                    return false;
                }
                vertexColor[verticeAdj] = cor;
                fila.add(verticeAdj);

                verticeAdj = highestSaturationAdjacent(findAdjacentyVertex(vertice));
            }
        }

        return true;
    }

    //Algoritmo 1 - OK
    public static Boolean BFS() {
        Integer verticeInicio = highestSaturationVertex();
        Integer cor = colorirVertice(verticeInicio, degreeSudoku);
        vertexColor[verticeInicio] = cor;

        List<Integer> fila = new ArrayList<>();
        fila.add(verticeInicio);

        while (!fila.isEmpty()) {
            Integer vertice = fila.get(0);
            fila.remove(0);

            for (Integer verticeAdj : findAdjacentyVertex(vertice)) {
                if(vertexColor[verticeAdj] == null) {
                    cor = colorirVertice(verticeAdj, degreeSudoku);
                    if(cor == null) {
                        //System.out.println("Não achou uma cor para o vértice "+verticeAdj);
                        return false;
                    }
                    vertexColor[verticeAdj] = cor;
                    fila.add(verticeAdj);
                }
            }
        }

        return true;
    }

    //Algoritmo 2 - OK
    public static Boolean DFS() {
        Integer verticeInicio = highestSaturationVertex();
        Integer cor = colorirVertice(verticeInicio, degreeSudoku);
        vertexColor[verticeInicio] = cor;

        Stack<Integer> pilha = new Stack<>();
        pilha.push(verticeInicio);

        while (!pilha.isEmpty()) {
            Integer v = pilha.pop();

            for (Integer verticeAdj : findAdjacentyVertex(v)) {
                if(vertexColor[verticeAdj] == null) {
                    cor = colorirVertice(verticeAdj, degreeSudoku);
                    if(cor == null) {
                        //System.out.println("Não achou uma cor para o vértice "+verticeAdj);
                        return false;
                    }
                    vertexColor[verticeAdj] = cor;

                    pilha.push(v);
                    pilha.push(verticeAdj);
                    break;
                }
            }
        }
        return true;
    }

    //OK
    public static Integer highestSaturationAdjacent(List<Integer> vertices) {
        Integer[] quantAdjacentesColoridos = new Integer[numbersVertex];
        //Selecionar o vertice com maior saturação
        Integer indexVertice = -1;
        Integer saturacaoVertice = 0;

        for (Integer v : vertices) {
            if(vertexColor[v] == null) {
                Integer grauSaturacao = grauSaturacaoVertice(v);
                quantAdjacentesColoridos[v] = grauSaturacao;
                indexVertice = 0;
            }
        }

        //Saber qual a menor saturação
        for (int i = 0; i < numbersVertex; i++) {
            if(quantAdjacentesColoridos[i] != null) {
                if(saturacaoVertice < quantAdjacentesColoridos[i]) {
                    indexVertice = i;
                    saturacaoVertice = quantAdjacentesColoridos[i];
                }
            }
        }

        //Buscar os vértices com mesma saturação
        List<Integer> verticesMesmaSaturação = new ArrayList<>();
        for (int i = 0; i < numbersVertex; i++) {
            if(quantAdjacentesColoridos[i] != null) {
                if(saturacaoVertice == quantAdjacentesColoridos[i]) {
                    verticesMesmaSaturação.add(i);
                }
            }
        }

        if(verticesMesmaSaturação.size() > 0) {
            //Sortear
            indexVertice = verticesMesmaSaturação.get(new Random().nextInt(verticesMesmaSaturação.size()));
        }

        if(indexVertice == -1)
            return null;

        return indexVertice;
    }

    //OK
    public static Integer highestSaturationVertex() {
        Integer[] quantAdjacentesColoridos = new Integer[numbersVertex];

        for(int v = 0; v < numbersVertex; v++) {
            if(vertexColor[v] == null) {
                Integer grauSaturacao = grauSaturacaoVertice(v);
                quantAdjacentesColoridos[v] = grauSaturacao;
            }
        }

        //Selecionar o vertice com maior saturação
        Integer indexVertice = -1;
        Integer saturacaoVertice = 0;

        for (int i = 0; i < numbersVertex; i++) {
            if(quantAdjacentesColoridos[i] != null) {
                if(saturacaoVertice < quantAdjacentesColoridos[i]) {
                    indexVertice = i;
                    saturacaoVertice = quantAdjacentesColoridos[i];
                }
            }
        }

        //Buscar os vértices com mesma saturação
        List<Integer> verticesMesmaSaturação = new ArrayList<>();
        for (int i = 0; i < numbersVertex; i++) {
            if(quantAdjacentesColoridos[i] != null) {
                if(saturacaoVertice == quantAdjacentesColoridos[i]) {
                    verticesMesmaSaturação.add(i);
                }
            }
        }

        if(verticesMesmaSaturação.size() > 0) {
            //Sortear
            indexVertice = verticesMesmaSaturação.get(new Random().nextInt(verticesMesmaSaturação.size()));
        }

        if(indexVertice == -1)
            return null;

        return indexVertice;
    }

    public static Integer grauSaturacaoVertice(Integer vertice) {
        Integer contCores = 0;
        List<Integer> adjacentes = findAdjacentyVertex(vertice);
        for (Integer adjacente : adjacentes) {
            if(vertexColor[adjacente] != null) {
                contCores++;
            }
        }
        return contCores;
    }

    //OK
    public static Integer colorirVertice(Integer vertice, Integer totalCores) {
        List<Integer> cores = new ArrayList<>();

        for (int cor = 1; cor <= totalCores; cor++) {
            if(podeColorir(vertice, cor)) {
                cores.add(cor);
            }
        }

        if(cores.size() == 0) {
            return null;
        }

        //Escolo uma cor aleatória do conjunto de cores que podem ser utilizadas
        // e retorno;
        int posicaoCor = new Random().nextInt(cores.size());
        return cores.get(posicaoCor);
    }

    //OK
    public static Boolean podeColorir(Integer vertice, Integer cor) {
        List<Integer> adjacentes = findAdjacentyVertex(vertice);
        for (Integer adjacente : adjacentes) {
            if (vertexColor[adjacente] == cor)
                return false;
        }
        return true;
    }

    //OK
    public static List<Integer> findAdjacentyVertex(Integer vertice) {
        List<Integer> adjacentVertex = new ArrayList<>();

        for(int i = 0; i < numbersVertex; i++) {
            boolean isAdjacente = graphAdjacencyMatrix[vertice][i] == 1 ? true : false;
            if(isAdjacente) {
                adjacentVertex.add(i);
            }
        }
        return adjacentVertex;
    }

    public static Integer[][] generateAdjacencyMatrix(int sudokuDegree, int linhasBloco, int colunasBloco) {
        int numbersVertex = sudokuDegree * sudokuDegree;
        int contVertex = 0;

        Integer[][] vertexLabelMatrix = new Integer[sudokuDegree][sudokuDegree];
        Integer[][] adjacencyMatrix = new Integer[numbersVertex][numbersVertex];

        for(int i = 0; i < sudokuDegree; i++) {
            for(int j = 0; j < sudokuDegree; j++) {
                vertexLabelMatrix[i][j] = contVertex;
                contVertex++;
            }
        }

        for(int i = 0; i < numbersVertex; i++) {
            for (int j = 0; j < numbersVertex; j++) {
                if(j >= (i/sudokuDegree)*sudokuDegree && j < (i/sudokuDegree)*sudokuDegree + sudokuDegree)
                    adjacencyMatrix[i][j] = 1;
                else if(j%sudokuDegree == i%sudokuDegree)
                    adjacencyMatrix[i][j] = 1;
                else
                    adjacencyMatrix[i][j] = 0;
            }

        }

        List<Integer> verticesAdjacentes = new ArrayList<>();

        int inicioColuna = 0;
        for(int i = colunasBloco ; i <= sudokuDegree; i += colunasBloco) {
            int inicioLinha = 0;
            for(int f = linhasBloco ; f <= sudokuDegree; f += linhasBloco) {

                for (int m = inicioLinha; m < f; m++) {

                    for (int k = inicioColuna; k < i; k++) {

                        Integer vertice = vertexLabelMatrix[m][k];
                        verticesAdjacentes.add(vertice);

                        //System.out.print(" "+vertice);
                    }
                }
                //System.out.println(" ");

                //Ligar os vértices
                for(int l = 0; l < verticesAdjacentes.size(); l++) {
                    int v1 = verticesAdjacentes.get(l);
                    for(int j = l; j < verticesAdjacentes.size(); j++) {
                        int v2 = verticesAdjacentes.get(j);
                        adjacencyMatrix[v1][v2] = adjacencyMatrix[v2][v1] = 1;
                    }
                }
                verticesAdjacentes.clear();

                inicioLinha = f;
            }
            inicioColuna = i;
        }

        for(int i = 0; i < numbersVertex; i++) {
            adjacencyMatrix[i][i] = 0;
        }

        return adjacencyMatrix;

    }

    public static void printMatrix(Integer[][] matrix) {
        for(int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(" "+matrix[i][j]);
            }
            System.out.println("");
        }
    }
}