package com.example.starpc;

import com.example.utils.Either;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class EitherSta {
	private Either<Pair<StaTerm, ServerContext>, TripleTup<ClientContext, ServerContext, StaTerm>> either;

	public EitherSta() {
		either = new Either<>();
	}
	
	public EitherSta(Either<Pair<StaTerm, ServerContext>, TripleTup<ClientContext, ServerContext, StaTerm>> either) {
		super();
		this.either = either;
	}

	public Either<Pair<StaTerm, ServerContext>, TripleTup<ClientContext, ServerContext, StaTerm>> getEither() {
		return either;
	}

	public void setEither(Either<Pair<StaTerm, ServerContext>, TripleTup<ClientContext, ServerContext, StaTerm>> either) {
		this.either = either;
	}

}
