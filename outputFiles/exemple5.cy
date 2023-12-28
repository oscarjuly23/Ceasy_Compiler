
// Bucle

func increment ( int valor ) -> int {
    valor = valor + 1
    return valor
}

func main ( ) -> void {
    int i = 0
    int counter
    while ( i < 5 ) {
        counter = increment ( counter )
        i = i + 1
    }
}
