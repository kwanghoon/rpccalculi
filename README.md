# Implementation of a Theory of RPC calculi

## A running example with the state-encoding calculi
## Run com.example.stacs.TypedCSStaInHttp

```
Enter a file name: ServerClient01.txt
(lam^s f. (lam^s x. x) (f 1)) (lam^c y. (lam^s z. z) y)

CLIENT: let f1 = Clo(_gf3, {}) in let x2 = Clo(_gf5, {}) in let r3 = Req(f1) (x2) in r3
CLIENT: let x2 = Clo(_gf5, {}) in let r3 = Req(Clo(_gf3, {})) (x2) in r3
CLIENT: let r3 = Req(Clo(_gf3, {})) (Clo(_gf5, {})) in r3
SERVER: let r = (Clo(_gf3, {})) (Clo(_gf5, {})) in r
SERVER: let r = let f4 = Clo(_gf1, {}) in let x5 = let f7 = Clo(_gf5, {}) in let x8 = 1 in let r11 = Call(Clo(_gf2, {f7})) (x8) in r11 in let r6 = (f4) (x5) in r6 in r
SERVER: let f4 = Clo(_gf1, {}) in let r = let x5 = let f7 = Clo(_gf5, {}) in let x8 = 1 in let r11 = Call(Clo(_gf2, {f7})) (x8) in r11 in let r6 = (f4) (x5) in r6 in r
SERVER: let r = let x5 = let f7 = Clo(_gf5, {}) in let x8 = 1 in let r11 = Call(Clo(_gf2, {f7})) (x8) in r11 in let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let x5 = let f7 = Clo(_gf5, {}) in let x8 = 1 in let r11 = Call(Clo(_gf2, {f7})) (x8) in r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let f7 = Clo(_gf5, {}) in let x5 = let x8 = 1 in let r11 = Call(Clo(_gf2, {f7})) (x8) in r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let x5 = let x8 = 1 in let r11 = Call(Clo(_gf2, {Clo(_gf5, {})})) (x8) in r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let x8 = 1 in let x5 = let r11 = Call(Clo(_gf2, {Clo(_gf5, {})})) (x8) in r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let x5 = let r11 = Call(Clo(_gf2, {Clo(_gf5, {})})) (1) in r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let r11 = Call(Clo(_gf2, {Clo(_gf5, {})})) (1) in let x5 = r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
CLIENT: let r3 = (Clo(_gf2, {Clo(_gf5, {})})) (1) in r3
CLIENT: let r3 = let y9 = (Clo(_gf5, {})) (1) in Ret(y9) in r3
CLIENT: let y9 = (Clo(_gf5, {})) (1) in let r3 = Ret(y9) in r3
CLIENT: let y9 = let f12 = Clo(_gf4, {}) in let x13 = 1 in let r14 = Req(f12) (x13) in r14 in let r3 = Ret(y9) in r3
CLIENT: let f12 = Clo(_gf4, {}) in let y9 = let x13 = 1 in let r14 = Req(f12) (x13) in r14 in let r3 = Ret(y9) in r3
CLIENT: let y9 = let x13 = 1 in let r14 = Req(Clo(_gf4, {})) (x13) in r14 in let r3 = Ret(y9) in r3
CLIENT: let x13 = 1 in let y9 = let r14 = Req(Clo(_gf4, {})) (x13) in r14 in let r3 = Ret(y9) in r3
CLIENT: let y9 = let r14 = Req(Clo(_gf4, {})) (1) in r14 in let r3 = Ret(y9) in r3
CLIENT: let r14 = Req(Clo(_gf4, {})) (1) in let y9 = r14 in let r3 = Ret(y9) in r3
SERVER: let r = (Clo(_gf4, {})) (1) in r
SERVER: let r = 1 in r
SERVER: 1
CLIENT: let r14 = 1 in let y9 = r14 in let r3 = Ret(y9) in r3
CLIENT: let y9 = 1 in let r3 = Ret(y9) in r3
CLIENT: let r3 = Ret(1) in r3
SERVER: let r11 = 1 in let x5 = r11 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let x5 = 1 in let r = let r6 = (Clo(_gf1, {})) (x5) in r6 in r
SERVER: let r = let r6 = (Clo(_gf1, {})) (1) in r6 in r
SERVER: let r6 = (Clo(_gf1, {})) (1) in let r = r6 in r
SERVER: let r6 = 1 in let r = r6 in r
SERVER: let r = 1 in r
SERVER: 1
CLIENT: let r3 = 1 in r3
CLIENT: 1
result: 1
Enter a file name: 
```

## A running example with the stateful calculi
### Run com.example.stacs.TypedCSStaInHttp

```
Enter a file name: ServerClient01.txt
(lam^s f. (lam^s x. x) (f 1)) (lam^c y. (lam^s z. z) y)

CLIENT: let f1 = Clo(_gf7, {}) in let x2 = Clo(_gf10, {}) in let r3 = Req(f1) (x2 Clo(_gf11, {})) in r3
CLIENT: let x2 = Clo(_gf10, {}) in let r3 = Req(Clo(_gf7, {})) (x2 Clo(_gf11, {})) in r3
CLIENT: let r3 = Req(Clo(_gf7, {})) (Clo(_gf10, {}) Clo(_gf11, {})) in r3
SERVER: (Clo(_gf7, {})) (Clo(_gf10, {}) Clo(_gf11, {}))
SERVER: (Clo(_gf5, {Clo(_gf10, {}) Clo(_gf11, {})})) (Clo(_gf6, {}))
SERVER: (Clo(_gf4, {Clo(_gf11, {}) Clo(_gf6, {})})) (Clo(_gf10, {}))
SERVER: (Clo(_gf3, {Clo(_gf10, {}) Clo(_gf11, {}) Clo(_gf6, {})})) (1)
SERVER: Call(Clo(_gf2, {Clo(_gf10, {}) Clo(_gf11, {}) Clo(_gf6, {})})) (1)
CLIENT: let r3 = (Clo(_gf2, {Clo(_gf10, {}) Clo(_gf11, {}) Clo(_gf6, {})})) (1) in r3
CLIENT: let r3 = let r10 = (Clo(_gf10, {})) (1) in Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r10 = (Clo(_gf10, {})) (1) in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r10 = let f12 = Clo(_gf8, {}) in let x13 = 1 in let r14 = Req(f12) (x13 Clo(_gf9, {})) in r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let f12 = Clo(_gf8, {}) in let r10 = let x13 = 1 in let r14 = Req(f12) (x13 Clo(_gf9, {})) in r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r10 = let x13 = 1 in let r14 = Req(Clo(_gf8, {})) (x13 Clo(_gf9, {})) in r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let x13 = 1 in let r10 = let r14 = Req(Clo(_gf8, {})) (x13 Clo(_gf9, {})) in r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r10 = let r14 = Req(Clo(_gf8, {})) (1 Clo(_gf9, {})) in r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r14 = Req(Clo(_gf8, {})) (1 Clo(_gf9, {})) in let r10 = r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
SERVER: (Clo(_gf8, {})) (1 Clo(_gf9, {}))
SERVER: (Clo(_gf9, {})) (1)
SERVER: 1
CLIENT: let r14 = 1 in let r10 = r14 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r10 = 1 in let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (r10) in r3
CLIENT: let r3 = Req(Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (1) in r3
SERVER: (Clo(_gf1, {Clo(_gf11, {}) Clo(_gf6, {})})) (1)
SERVER: (Clo(_gf6, {})) (1 Clo(_gf11, {}))
SERVER: (Clo(_gf11, {})) (1)
SERVER: 1
CLIENT: let r3 = 1 in r3
CLIENT: 1
Result: 1
Enter a file name: 
```
