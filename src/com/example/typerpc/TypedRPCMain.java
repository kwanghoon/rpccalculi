package com.example.typerpc;

public class TypedRPCMain {

	public static Type subst(Type t, int i, Type ty) {
		if (t instanceof IntType) {
			IntType intType = (IntType) t;
			
			return intType;
		}
		else if (t instanceof VarType) {
			VarType varType = (VarType) t;
			
			if (i == varType.getVar())
				return ty;
			else
				return varType;
		}
		else if (t instanceof FunType) {
			FunType funType = (FunType) t;
			
			Type left = subst(funType.getLeft(), i, ty);
			Type right = subst(funType.getRight(), i, ty);
			
			FunType retFunType = new FunType(left, funType.getLoc(), right);
			
			return retFunType;
		}
		else return null;
	}
	
	public static Type substTyTyLoc(Type t, int i, TypedLocation tyloc) {
		if (t instanceof IntType) {
			IntType intType = (IntType) t;
			
			return intType;
		}
		else if (t instanceof VarType) {
			VarType varType = (VarType) t;
			
			return varType;
		}
		else if (t instanceof FunType) {
			FunType funType = (FunType) t;
			TypedLocation funTypedLocation = funType.getLoc();
			
			if (funTypedLocation instanceof LocType) {
				LocType locType = (LocType) funTypedLocation;
				
				return funType;
			}
			else if (funTypedLocation instanceof LocVarType) {
				LocVarType locVarType = (LocVarType) funTypedLocation;
				
				if (i == locVarType.getVar())
					return new FunType(funType.getLeft(), tyloc, funType.getRight());
				else
					return funType;
			}
			else return null;
		}
		else return null;
	}
	
	public static TypedLocation substTyLoc(TypedLocation tyloc, int j, TypedLocation jtyloc) {
		if (tyloc instanceof LocVarType) {
			LocVarType locVarType = (LocVarType) tyloc;
			
			if (locVarType.getVar() == j)
				return jtyloc;
			else
				return locVarType;
		}
		else if (tyloc instanceof LocType) {
			LocType locType = (LocType) tyloc;
			
			return locType;
		}
		else return null;
	}
	
	public static void main(String[] args) {
		
	}
}
