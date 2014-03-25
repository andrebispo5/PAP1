package ist.meic.pa;

import ist.meic.pa.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.*;

public class Inspector {
	
	private boolean running ;
	private Navigator navigator;
	
	public Inspector(){
		navigator = new Navigator();
		running=true;
	}
	
	
	public void inspect(Object object){
		printWelcomeMsg();
		inspectObject(object);
		listenConsole();
	}

	
	public void inspectObject(Object object){
		TypeValidator tv = new TypeValidator(); 
		navigator.add(object);
		Class<?> c = object.getClass();
		if(tv.isPrimitiveWrapper(c)){
			navigator.add(object);
			System.err.println(object);
		}else if(c.isArray()){
			inspectArray(object);
		}else{
			System.err.println(object.toString() + " is an instance of class "  + c.getName() );
			System.err.println("\n-----FIELDS-----");
			printFields(object, c);
			System.err.println("\n-----METHODS-----");
			printMethods(object, c);
		}
	}


	private void inspectArray(Object object) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		int length = Array.getLength(object);
		System.err.println("Inspecting an Array of length "+ length +", press v to view elements or enter number.");
		String command;
		try {
			command = in.readLine();
			if(command.contains("v")){
				printArray(object, length);
			}else{
				int position = Integer.parseInt(command);
				inspectObject(Array.get(object, position));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void printArray(Object object, int length) {
		int colCount = 0;
		for (int i = 0; i < length; i ++) {
			Object arrayElement = Array.get(object, i);
			System.err.print("["+i+"]"+arrayElement + " ");
			if(colCount ==10){
				System.err.print("\n");
		        colCount=0;
			}
			colCount++;
		}
	}

	private void printNavigation() {
		navigator.printNavigationBar();
	}


	private void listenConsole() {
		String command = null;
		String[] commandSplit = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(running){
			printNavigation();
			System.err.print(">");
			try {
				command = in.readLine();
				commandSplit = command.split(" ");
				Class<? extends Command> cmd = Class.forName("ist.meic.pa.command." + commandSplit[0] + "Command").asSubclass(Command.class);
				cmd.newInstance().execute(this, commandSplit);
			} catch (IOException e) {
				System.err.println("Input not working properly please restart.");
			} catch (ClassNotFoundException e) {
				System.err.println("Command not found. Try again or enter h for help.");
			} catch (InstantiationException e) {
				System.err.println("Command not found. Try again or enter h for help.");			
			} catch (IllegalAccessException e) {
				System.err.println("Command not found. Try again or enter h for help.");
			}
		}
		
	}

	private void printFields(Object object, Class<? extends Object> c){
		Field[] fields = c.getDeclaredFields();
		for (int j=0;j<fields.length;j++){
			fields[j].setAccessible(true);
			String modfs = Modifier.toString(fields[j].getModifiers());
			String fieldName = fields[j].getName();
			Class<?> type = fields[j].getType();
			String fieldType;
			if (type.isArray())
				fieldType = type.getComponentType().getName() + "[] ";
			else
				fieldType=type.getName();
			if (!(modfs.equals("")))
				System.err.print(modfs + " ");
			try {
				System.err.println(fieldType + " " + fieldName + " " + "=" + " " +fields[j].get(object));
			} catch (IllegalArgumentException e) {
				System.err.println("Field cannot be accessed.");
			} catch (IllegalAccessException e) {
				System.err.println("Field cannot be accessed.");
			}
		}
		Class<?> cl = c.getSuperclass();
		if(cl != null)
			printFields(object, cl);
	}
	
	public void quit(){
		running=false;
	}
	
	private void printMethods(Object object, Class<? extends Object> c){
		Method[] meth = c.getDeclaredMethods();
		for (int j=0;j<meth.length;j++){
			meth[j].setAccessible(true);
			String modfs = Modifier.toString(meth[j].getModifiers());
			String methodName = meth[j].getName();
			Class<?> type = meth[j].getReturnType();
			String methReturnType;
			if (type.isArray())
				methReturnType = type.getComponentType().getName() + "[] ";
			else
				methReturnType=type.getName();
			Class<?> param[] = meth[j].getParameterTypes();
			String parameters = argumentsToString(param);
			if (!(modfs.equals("")))
				System.err.print(modfs + " ");
			System.err.println(methReturnType + " " + methodName + "(" + " " + parameters + " );"); 

		}
		Class<?> cl = c.getSuperclass();
		if(cl != Object.class)
			printMethods(object, cl);
	}


	private String argumentsToString(Class<?>[] param) {
		String parameters = "";
		for ( int i =0; i<param.length;i++){
			if (param[i].isArray())
				parameters += param[i].getComponentType().getName() + "[] ";
			else
				parameters+=param[i].getName();
			if (i < param.length-1)
				parameters +=", ";
		}
		return parameters;
	}



	public Navigator getNavigator() {
		return this.navigator;
	}

	private void printWelcomeMsg() {
		System.err.println("JAVA INSPECTOR v1.0");
		System.err.println("-------------------------------------------------------");
		System.err.println("");
	}
}
