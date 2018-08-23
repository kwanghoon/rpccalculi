package com.example.lib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.rpc.Token;

public class CommonParserUtil {
	// file names
	private String fGrammarRules;
	private String fActionTable;
	private String fGotoTable;

	// Lexer part
	private int lineno;
	private String endOfTok;
	private Object objEndOfTok;

	private BufferedReader br;
	private ArrayList<String> lineArr;
	private ArrayList<Terminal> lexer;

	private LinkedHashMap<String, TokenBuilder> tokenBuilders;

	// Parser part
	private String startSymbol;

	private ArrayList<String> action_table;
	private ArrayList<String> goto_table;
	private HashMap<Integer, String> grammar_rules;

	private LinkedHashMap<String, TreeBuilder> treeBuilders;

	private Stack<Stkelem> stack;

	private int productionRuleIdx;

	public CommonParserUtil() throws IOException {
		super();
		this.lexer = new ArrayList<Terminal>();

		stack = new Stack<>();

		action_table = new ArrayList<>();
		goto_table = new ArrayList<>();
		grammar_rules = new HashMap<>();

		treeBuilders = new LinkedHashMap<>();
		tokenBuilders = new LinkedHashMap<>();
	}
	
	public Object get(int i) {
		String productionRuleStr = grammar_rules.get(productionRuleIdx);
		String[] splitRule = productionRuleStr.split("[\t ]");
		int length = splitRule.length - 2;

		int last_stack_tree_index = stack.size() - 1;
		int offset = (length * 2) - ((i - 1) * 2 + 1);
		Nonterminal nt = (Nonterminal) stack.get(last_stack_tree_index - offset);

		return nt.getTree();
	}

	public String getText(int i) {
		String productionRuleStr = grammar_rules.get(productionRuleIdx);
		String[] splitRule = productionRuleStr.split("[\t ]");
		int length = splitRule.length - 2;

		int last_stack_tree_index = stack.size() - 1;
		int offset = (length * 2) - ((i - 1) * 2 + 1);
		Terminal nt = (Terminal) stack.get(last_stack_tree_index - offset);

		return nt.getSyntax();
	}

	public void ruleStartSymbol(String startSymbol) {
		this.startSymbol = startSymbol;
	}

	public void rule(String productionRule, TreeBuilder tb) {
		treeBuilders.put(productionRule, tb);
	}

	public void lex(String regExp, TokenBuilder tb) {
		tokenBuilders.put(regExp, tb);
	}

	public void lexEndToken(String regExp, Object objEndOfTok) {
		endOfTok = regExp;
		this.objEndOfTok = objEndOfTok;
	}

	public void Lexing(Reader r) throws IOException, LexerException {
		br = new BufferedReader(r);
		lineArr = new ArrayList<>();

		String read_string = br.readLine();

		while (true) {
			String next_string = br.readLine();

			if (next_string != null) {
				lineArr.add(read_string + "\n");
				read_string = next_string;
			}
			else {
				lineArr.add(read_string);
				break;
			}
		}

		lineno = 1;
		TokenBuilder tb;

		Object[] keys = tokenBuilders.keySet().toArray();

		for (int idx = 0; idx < lineArr.size(); idx++) {
			String line = lineArr.get(idx);
			String str = "";

			// pattern matching
			int front_idx = 0;

			while (front_idx < line.length()) {
				int i;
				for (i = 0; i < keys.length; i++) {
					String regExp = (String) keys[i];
					Pattern p = Pattern.compile(regExp);
					Matcher matcher = p.matcher(line).region(front_idx, line.length());

					if (matcher.lookingAt()) {
						int startIdx = matcher.start();
						int endIdx = matcher.end();

//						System.out.println(startIdx +", " + endIdx);
						
						str = line.substring(startIdx, endIdx);
						matcher.region(endIdx, line.length());

						tb = tokenBuilders.get(regExp);
						if (tb.tokenBuilder(str) != null) {
							lexer.add(new Terminal(str, tb.tokenBuilder(str), startIdx, lineno));
						}

						str = "";

						front_idx = endIdx;
						break;
					}
				}
				if (i >= keys.length)
					throw new LexerException("No Pattern matching " + front_idx + ", " + line.substring(front_idx));
			}

			lineno++;
		}

		Terminal epsilon = new Terminal(endOfTok, objEndOfTok, -1, -1);
		lexer.add(epsilon);
	}

	public Object Parsing(Reader r) throws ParserException, IOException, LexerException {
		readInitialize();
		
		Lexing(r);

		stack.clear();
		stack.push(new ParseState("0"));
		Object tree = null;

		while (!lexer.isEmpty()) {
			ParseState currentState = (ParseState) stack.lastElement();
			Terminal currentTerminal = lexer.get(0);

			ArrayList<String> data_arr = Check_state(currentState, currentTerminal, lexer);
			String order = data_arr.get(0); // Accept | Reduce | Shift

			switch (order) {
			case "Accept":
				lexer.remove(0);
				return ((Nonterminal) stack.get(1)).getTree();
			case "Shift":
				String state = data_arr.get(1);
				stack.push(currentTerminal);
				stack.push(new ParseState(state));
				lexer.remove(0);
				break;
			case "Reduce":
				int grammar_rule_num = Integer.parseInt(data_arr.get(1));
				productionRuleIdx = grammar_rule_num;

				String[] reduce_arr = grammar_rules.get(grammar_rule_num).split("->");
				String lhs = reduce_arr[0].trim();
				String rhs;
				int rhsCount;

				if (!(reduce_arr.length == 1)) // -> "empty"
				{
					rhs = reduce_arr[1].trim();
					rhsCount = rhs.split(" ").length; // *2 => pop count.
				}
				else {
					rhs = "";
					rhsCount = 0;
				}

				int last_stack_tree_index = stack.size() - 1;

				TreeBuilder tb = treeBuilders.get(grammar_rules.get(grammar_rule_num));

				if (tb != null) {
					tree = tb.treeBuilder();
				}
				else {
					throw new ParserException("Unexpected grammar rule " + grammar_rule_num);
				}

				// pop stack
				while (rhsCount != 0) {
					stack.pop();
					stack.pop();
					rhsCount--;
				}

				currentState = (ParseState) stack.lastElement();

				stack.push(new Nonterminal(tree));
				stack.push(get_st(currentState, lhs, lexer));
				break;
			}
		}
		throw new ParserException("Empty Token in Lexer");
	}

	private void readInitialize() throws IOException {
		try {
			FileReader grammarFReader = new FileReader("grammar_rules.txt");
			FileReader actionFReader = new FileReader("action_table.txt");
			FileReader gotoFReader = new FileReader("goto_table.txt");

			BufferedReader grammarBReader = new BufferedReader(grammarFReader);
			BufferedReader actionBReader = new BufferedReader(actionFReader);
			BufferedReader gotoBReader = new BufferedReader(gotoFReader);

			String tmpLine;

			while ((tmpLine = grammarBReader.readLine()) != null) {
				// grammarNumber: grammar
				String[] arr = tmpLine.split(":");

				int grammerNum = Integer.parseInt(arr[0].trim());
				String grammer = arr[1].trim();

				grammar_rules.put(grammerNum, grammer);
			}

			while ((tmpLine = actionBReader.readLine()) != null) {
				action_table.add(tmpLine);
			}

			while ((tmpLine = gotoBReader.readLine()) != null) {
				goto_table.add(tmpLine);
			}
		}
		catch (FileNotFoundException e) {
			createGrammarRules();
		}
	}

	private void createGrammarRules() throws IOException {
		// CFG "L'" [
		// 		ProductionRule "L'" [Nonterminal "L"],
		// 		ProductionRule "L" [Nonterminal "E"],
		// 		ProductionRule "L" [Terminal "lam", Terminal "loc", Terminal "id", Terminal ".", Nonterminal "L"],
		// 		ProductionRule "E" [Nonterminal "E", Nonterminal "T"],
		// 		ProductionRule "E" [Nonterminal "T"],
		// 		ProductionRule "T" [Terminal "id"],
		// 		ProductionRule "T" [Terminal "num"],
		// 		ProductionRule "T" [Terminal "(", Nonterminal "L", Terminal ")"]
		// ]
		Object[] objGrammar = treeBuilders.keySet().toArray();

		// HashMap은 안됨 Key가 겹쳐서 덮어씌워짐
		ArrayList<String> nonterminals = new ArrayList<>();

		// nonterminal setting
		for (int i = 0; i < objGrammar.length; i++) {
			String grammar = (String) objGrammar[i];
			String[] data = grammar.split(" -> "); // symbol -> g1 g2 g3 ...

			if (!nonterminals.contains(data[0].trim())) {
				nonterminals.add(data[0].trim());
			}
		}

		String fileContent = "CFG \"" + startSymbol + "\" [\n";

		for (int i = 0; i < objGrammar.length; i++) {
			String grammar = (String) objGrammar[i];
			String[] data = grammar.split(" -> ");

			fileContent += "\tProductionRule \"" + data[0] + "\" [";

			// data[0] 는 ProductionRule 태그 붙이기
			// data[1] 은 공백으로 나눠 Nonterminal Terminal 판단 필요
			String[] tok = data[1].split("[ \t\n]");

			for (int j = 0; j < tok.length; j++) {
				if (nonterminals.contains(tok[j])) { // 현재 token이 Nonterminal인 경우
					fileContent += "Nonterminal \"";
				}
				else {
					fileContent += "Terminal \"";
				}

				fileContent += tok[j] + "\"";

				if (j < tok.length - 1) {
					fileContent += ", ";
				}
			}

			fileContent += "]";
			if (i < objGrammar.length - 1) {
				fileContent += ",\n";
			}
			else
				fileContent += "\n";

		}

		fileContent += "]";

		String directory = System.getProperty("user.dir");
		String grammarPath = directory + "\\mygrammar.grm";
		
		// file 출력
		try {
			PrintWriter writer = new PrintWriter(grammarPath);
			writer.println(fileContent);
			writer.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String grammarRulesPath = directory + "/grammar_rules.txt";
		String actionTablePath = directory + "/action_table.txt";
		String gotoTablePath = directory + "/goto_table.txt";
		
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", directory + "/genlrparser-exe.exe",
								"\"" + grammarPath + "\" -output \"" + grammarRulesPath + "\" \"" + actionTablePath + "\" \"" + gotoTablePath + "\"");
		try {
			Process p = pb.start();
			readInitialize();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ParseState get_st(ParseState current_state, String index, ArrayList<Terminal> Tokens)
			throws FileNotFoundException, ParserException {
		int count = 0;

		String start_state;
		int location = 0;

		while (count < goto_table.size()) {
			String[] st_tr_arr = goto_table.get(count).split("[\t ]");
			while (location < st_tr_arr.length) {
				start_state = st_tr_arr[location];
				if (current_state.toString().equals(start_state)) {
					location++;
					if (st_tr_arr[location].equals(index)) {
						location++;
						if (!st_tr_arr[location].equals(""))
							return new ParseState(st_tr_arr[location]);
						else
							break;
					}
					else
						break;
				}
				else
					break;
			}
			count++;
			location = 0;
		}
		StringBuilder sb = new StringBuilder();

		sb.append(
				"Expect Trans Table content is \"" + current_state.toString() + " " + index + " <Destination State>\"");
		sb.append("\n");
		sb.append("but didn't found at Trans Table... Plz Check it");
		sb.append("\n");

		// For locating the parsing error.
		int err_ch_index = -1;
		int err_line_index = -1;
		String culprit = "no hint";

		if (Tokens.isEmpty() == false) {
			Terminal t = Tokens.get(0);
			err_ch_index = t.getChIndex();
			err_line_index = t.getLineIndex();
			culprit = t.getSyntax();
		}

		throw new ParserException("Line : Char : " + "Parsing error (state not found)" + sb.toString(), culprit,
				err_line_index, err_ch_index);
	}

	private ArrayList<String> Check_state(ParseState current_state, Terminal terminal, ArrayList<Terminal> Tokens)
			throws ParserException {
		int index = 0;
		while (index < action_table.size()) {
			String action_table_str = action_table.get(index);
			// state_num Terminal [Accept | Reduce grammar_rule_num | Shift state_num]
			String[] data = action_table_str.split("[\t ]");
			ArrayList<String> return_data = new ArrayList<>();

			if (current_state.toString().equals(data[0])) {
				int index1 = 1;
				Token index_Token;

				while (data[index1].equals("") || data[index1].equals("\t")) {
					index1++;
					continue;
				}

				index_Token = Token.findToken(data[index1]);

				if (terminal.getToken() == index_Token) {
					String return_string = new String();
					for (int i = index1 + 1; i < data.length; i++) {
						if (data[i].equals(""))
							continue;

						return_data.add(data[i]);
					}
					return return_data;
				}
			}
			index++;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Expect Parsing table content is \"" + current_state.toString() + " " + terminal.toString()
				+ " <Shift/Reduce/Accecpt>\"");
		sb.append("\n");

		sb.append("but didn't found at Parsing Table... Plz Check it");
		sb.append("\n");
		sb.append("[" + terminal.getLineIndex() + "," + terminal.getChIndex() + "]");
		sb.append("\n");

		// For locating the parsing error.
		int err_ch_index = -1;
		int err_line_index = -1;
		String culprit = "no hint";

		if (Tokens.isEmpty() == false) {
			Terminal t = Tokens.get(0);
			err_ch_index = t.getChIndex();
			err_line_index = t.getLineIndex();
			culprit = t.getSyntax();
		}

		throw new ParserException("Line " + terminal.getLineIndex() + " : Char " + terminal.getChIndex() + " : "
				+ "Parsing error " + sb.toString(), culprit, err_line_index, err_ch_index);
	}
}
