
int boo(int aa, double bb) { 
    int kk = aa + bb;
    double res = 1;
    for (int ii = kk; ii > 0; ii--) {
        res *= ii;
    }
    return res;
}

void method_name_with_underscore() {
    method_name_with_underscore();
}
