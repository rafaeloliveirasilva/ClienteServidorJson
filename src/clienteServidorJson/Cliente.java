package clienteServidorJson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Cliente {
	
	private Socket         socket;
	private BufferedReader recebe;
	private PrintStream    ENVIA;	
	private boolean        inicializado;
	private boolean        executando;	
	private int            controle = 0;
	private boolean        votou;
	
	ArrayList<Candidato> listaCandidato = null ;
	
	//Estabelce conexao com o servidor
	public void abreConexao() throws IOException{
		try {
			socket = new Socket("localhost", 2525);
			recebe = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		    ENVIA  = new PrintStream(socket.getOutputStream());
			
		} catch (Exception e) {
			fecha();
			System.out.println(e);
		}
	}
	
	//Fecha a conexão com o servidor
	public void fecha() throws IOException{
		if(recebe != null)
			recebe.close();
		
		if(ENVIA != null)
			ENVIA.close();
		
		if(socket != null)
			socket.close();
		
	}
	//Carrega a lista de candidatos que está no servidor
	public void carregaCandidatos(String opCod) throws JsonSyntaxException, IOException{		
		
	    if(controle == 0){
	    	abreConexao();	
	    	
	    	Gson gson =  new Gson();
		    listaCandidato = new ArrayList<>();
	    	
			ENVIA.println(opCod);
			ENVIA.flush();

			Type type = new TypeToken<ArrayList<Candidato>>(){}.getType();
			listaCandidato = gson.fromJson(recebe.readLine(), type);
			
			System.out.println("\nLista de Candidatos carregada.\n");
			fecha();
			controle++;
		}
		else
			System.out.println("\nLista de Candidatos ja foi carreda. Ja pode votar.\n");
		
		//Mostra as informações sobre todos os candidatos
		System.out.println("Lista de candidatos");
		for(int i=0; i < listaCandidato.size() ; i++){
			System.out.println("Codigo: "           + listaCandidato.get(i).getCodigo_votacao() + 
			                   ", Nome: "           + listaCandidato.get(i).getNome_candidato() +  
			                   ", Pardido: "        + listaCandidato.get(i).getPartido() +
			                   ", Numero de Votos:" + listaCandidato.get(i).getNum_votos()  );
		}
		System.out.println("\n");
		
	}
	
	public void executaVoto() throws IOException{
		
		if(controle == 0){ //Checa se a lista de candidato foi carregada 
			System.out.println("Por favor carregue a lista de cadidatos.\n");
		}
		else{
			boolean codigoEncontrado = false;
			int posi = 0 ;
			
			System.out.println("Digite o codigo do candidato que deseja votar.\n");
			Scanner scanner = new Scanner(System.in);
			int codigo = scanner.nextInt();
			
			//Procura o cadidato na lista
			for(int j=0; j<listaCandidato.size(); j++){
				if(listaCandidato.get(j).getCodigo_votacao() == codigo){
					posi = j;
					codigoEncontrado = true;
				}
			}
			if(codigoEncontrado == true){
				
				System.out.println("Esse é o seu candidato?");
				System.out.println("Codigo: "  + listaCandidato.get(posi).getCodigo_votacao() + "\n" +
		                           "Nome: "    + listaCandidato.get(posi).getNome_candidato() + "\n" +
		                           "Pardido: " + listaCandidato.get(posi).getPartido() +"\n");
				
				System.out.println("Aperte s para confirmar seu voto");
				System.out.println("Aperte n para corrigir seu voto");
				char c = (char)System.in.read();
				
				if(c == 's'){
					int votos = listaCandidato.get(posi).getNum_votos();
					votos++;
					listaCandidato.get(posi).setNum_votos(votos);
					System.out.println("Seu voto registrado com sucesso\n");
					votou = true;
					
					
					for(int i=0; i < listaCandidato.size() ; i++){
						System.out.println("Codigo: "           + listaCandidato.get(i).getCodigo_votacao() + 
						                   ", Nome: "           + listaCandidato.get(i).getNome_candidato() +  
						                   ", Pardido: "        + listaCandidato.get(i).getPartido() +
						                   ", Numero de Votos:" + listaCandidato.get(i).getNum_votos()  );
					}
				}
			}
			else{
				System.out.println("Por favor, entre com um codigo de candidato valido.\n");
		    }
	   }
	}
	
	public void enviaVotosServidor(String opCod){
		
		if(votou == true){
			
			Gson gson =  new Gson();

			try {
				abreConexao();	
				
				String jsonCandidato = gson.toJson(listaCandidato);
				
				System.out.println(jsonCandidato);
				
				ENVIA.println(jsonCandidato);
			
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else
			System.out.println("Nenhum voto foi registrado");
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		Cliente cliente = new Cliente();
		String opCod;
		
		boolean var = true;
		while(var){
			System.out.println("Entre com uma das opções abaixo.");
			System.out.println("1 Votar.");
			System.out.println("2 Votar em Branco.");
			System.out.println("3 Votar Nulo.");
			System.out.println("999 Carrega a lista de candidatos do servidor.");
			System.out.println("888 Para encerrar a votação.\n");
			
			Scanner scanner = new Scanner(System.in);
			opCod = scanner.nextLine();
			
			if("1".equals(opCod)){
				cliente.executaVoto();
			}
			if("999".equals(opCod)){
				cliente.carregaCandidatos(opCod);
			}
			if("888".equals(opCod)){
				cliente.enviaVotosServidor(opCod);
				var = false;
			}
		}
	}
}
