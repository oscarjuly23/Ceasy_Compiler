
// fibbonacci iteratiu

func fb ( int num ) -> int {
    int prev = 1
    int prevPrev
    int res
    int count = 1

    while ( count < num ) {
        res = prevPrev + prev
        prevPrev = prev
        prev = res
        count = count + 1
    }
    return res
}

func main ( ) -> void {

    int a = fb ( 6 )

}
