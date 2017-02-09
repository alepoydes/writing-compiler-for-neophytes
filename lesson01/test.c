#include <stdio.h>
void main() {
    const int N=100;
    int sum=0;
    int n;
    for(n=0;n<N;n++) sum+=n;
    printf("Sum = %d\n", sum);
}