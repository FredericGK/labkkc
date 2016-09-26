import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.State;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.omg.PortableServer.POAPackage.ServantAlreadyActive;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Tp1Inf2015 {
	private static boolean informationValide = true ;
	private static int verification = 0 ;

	private static String jsonTxt ;
	private static JSONObject jsonObject ;
	private static JSONArray jsonArray ;
	
	private static String cycle;
	private static String[] categories ;
	private static int heureCyclePrec;
	private static int cumulHeures ;
	private static String[] categorieACumuler = {"cours" , "atelier" , "séminaire" , "colloque" , "conférence" , "lecture dirigée"} ;
	private static int cumulParCategorie ;
	private static String[] description;
	private static String[] datesActivites ;
	private static int nbHeuresGroupeDisc ;
	private static int nbHeuresPresentation ;
	private static int nbHeuresProjetRecher ;
	private static int nbHeuresRedaction ;
	private static int[] heures ;
	private static String[] categoriesValides = { "cours" , "atelier" , "séminaire" , "colloque", "conférence"
			, "lecture dirigée" , "présentation" , "groupe de discussion"
			, "projet de recherche", "rédaction professionnelle"} ; 

	private static boolean verificationCycle = true ;
	private static boolean verificationDate = true ;
	private static boolean verificationCategorie = true ;
	private static boolean verificationHeureSupp = true ;
	private static boolean verificationCumul = true ;
	private static boolean verificationCumulCategorie = true ;
	private static boolean verificationHeure = true ;
	private static boolean verificationHeureActivite = true ;

	private static String MSG_ERR_CYCLE = "Uniquement le cycle 2014-2016 est supporté ." ;
	private static String MSG_ERR_DATE_HORS_CYCLE = "Toutes les activités déclarées pour le " + 
													"cycle 2014-2016 doivent avoir été complétées " + 
													"entre le 1er avril 2014 et le 1er avril 2016 ." ;
	private static String MSG_ERR_CATEGORIE = "L'activité XXX est dans une catégorie non reconnue. Elle sera ignorée ." ;
	private static String MSG_ERR_NOMBRE_HEURES_SUPP = "Le nombre d'heures transférés du cycle precedent est supérieur à 7 ." ;
	private static String MSG_ERR_HEURE_FORMATION_INS = "Un minimum de 40 heures de formation doivent être déclarées dans le cycle ." ;
	private static String MSG_ERR_HEURE_MIN_PAR_CAT = "La somme des heures des activités appartenants à aux catégories " + 
													  "cours, atelier, séminaire, colloque, conférence, lecture dirigée " + 
													  "doit être supérieure ou égale à 17 heures ." ;
	private static String MSG_ERR_VALEUR_HEURES = "Les heures d'une activité doivent être supérieures ou égales à 1 ." ;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ;

	private static String jsonSortie ;

	public static void main(String[] args) {
		initialiserJson();

		//CYCLE
		cycle = getCycle() ;
		verifierCycle ("2014-2016") ;

		//DESCRIPTIONS
		description = getDescriptions() ;

		//DATES ACTIVITÉS
		datesActivites = getDatesActivités() ;
		try {
			verifierDates(sdf.parse("2014-04-01"), sdf.parse("2016-04-01"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//CATEGORIES 
		categories = getCategories() ;
		verifierCategories(categories, categoriesValides) ;

		//HEURES SUPP
		heureCyclePrec = getHeureCyclePrec() ;
		verifierHeuresSupp(heureCyclePrec);

		//HEURES CUMULÉES
		cumulHeures = getCumul() ;
		verifierCumul(cumulHeures);

		//CUMUL PAR CATEGORIES
		cumulParCategorie = getCumulParCategorie(categorieACumuler);
		verifierCumulParCategorie(cumulParCategorie);

		//CATEGORIE PRESENTATION
		nbHeuresPresentation = getHeureActivite("présentation") ;
		if (verificationCategorie(categoriesValides, "presentation") == true) {
			verifierHeureActivite("presentation");
		};
		
		//CATEGORIE GROUPE DE DISCUSSION
		nbHeuresGroupeDisc = getHeureActivite("groupe de discussion") ;
		if (verificationCategorie(categoriesValides, "groupe de discussion") == true) {
			verifierHeureActivite("groupe de discussion");
		};
		
		//CATEGORIE PROJET DE RECHERCHE
		nbHeuresProjetRecher = getHeureActivite("projet de recherche") ;
		if (verificationCategorie(categoriesValides, "projet de recherche") == true) {
			verifierHeureActivite("projet de recherche");
		};
		
		//CATEGORIE REDACTION
		nbHeuresRedaction = getHeureActivite("redaction") ;
		if (verificationCategorie(categoriesValides, "redaction") == true) {
			verifierHeureActivite("redaction");
		};
		
		//HEURES
		heures = getHeures() ;
		verifierHeures(heures);
		
		
		System.out.println(jsonSortie);
	}

	public static void initialiserJson() {
		try {
			jsonTxt = Utf8File.loadFileIntoString("/Users/Fredy/Desktop/Projet 1/json.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		jsonObject = (JSONObject) JSONSerializer.toJSON(jsonTxt);
	}

	public static String getCycle () {
		cycle = (String) jsonObject.get("cycle") ;
		return cycle ;
	}

	public static void verifierCycle (String cycleVerification){
		verification++;
		jsonSortie = "Verification " + (verification) + " : " ;
		if (cycle.equals(cycleVerification) == false ){
			jsonSortie = jsonSortie + "Erreur Cycle " + MSG_ERR_CYCLE ;
			verificationCycle = false ;
		} else {
			jsonSortie = jsonSortie + "Cycle valide \n" ;
		}
	}

	public static String[] getDatesActivités (){
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites") ;
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		datesActivites = new String[tailleArray] ;
		for (int i = 0 ; i < tailleArray ; i++){
			json = jsonArray.getJSONObject(i) ;
			datesActivites[i] = json.getString("date") ;
		}
		return datesActivites ;
	}

	public static void verifierDates (Date dateMin , Date dateMax){
		verification++;
		jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
		Date date = null ;

		int[] verif = new int [datesActivites.length] ;
		for (int i = 0 ; i < datesActivites.length ; i++) {
			try {
				date = sdf.parse(datesActivites[i]) ;
				if (date.before(dateMax) && date.after(dateMin)) {
					verif[i] = 0 ;
				} else {
					verif[i] = 1 ;
					verificationDate = false ;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (verificationDate = false){
			jsonSortie = jsonSortie + MSG_ERR_DATE_HORS_CYCLE ;
		} else {
			jsonSortie = jsonSortie + "Date valide : " ;
		}
		for (int i = 0 ; i < verif.length ; i++) {
			jsonSortie = jsonSortie + verif[i] + " - " ;
		} 
		jsonSortie = replaceCharAt(jsonSortie, jsonSortie.length()-2, ' ') + "\n" ;
	}

	public static String[] getCategories (){
		String[] categories ;
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites") ;
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		categories = new String[tailleArray] ;
		for (int i = 0 ; i < tailleArray ; i++){
			json = jsonArray.getJSONObject(i) ;
			categories[i] = json.getString("categorie") ;
		}
		return categories;
	}

	public static int[] getHeures () {
		int[] heures ;
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites") ;
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		heures = new int[tailleArray] ;
		for (int i = 0 ; i < tailleArray ; i++){
			json = jsonArray.getJSONObject(i) ;
			categories[i] = json.getString("heures") ;
		}
		return heures ;
	}
	
	public static void verifierHeures (int[] heures) {
		boolean test  = true ;
		verification++;
		jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
		for (int i = 0 ; i < heures.length ; i ++){
			if (heures[i] < 1) {
				test = false ;
			} 
		}
		
		if (test = false) {
			jsonSortie = jsonSortie + "Erreur, une heure ne peut pas être négative ou égale à 0 . \n" ;
			verificationHeure = false ;
		} else {
			jsonSortie = jsonSortie + "Heures valides" ; 
		}
	}
	
	public static void verifierCategories(String[] categorie, String[] categoriesValides) {
		int f = 0;
		verification++;
		jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
		int[] verifGeneral = new int[categorie.length] ;
		for (int i = 0 ; i < categorie.length ; i++){
			verifGeneral[i] = 0 ;
		}
		for (int i = 0 ; i < categorie.length ; i++) {
			for (int k = 0 ; k < categoriesValides.length ; k++){
				if (categorie[i].contains(categoriesValides[k])) {
					verifGeneral[i] = 1 ;
				} 
			}
		}

		for (int i = 0 ; i < verifGeneral.length ; i++){
			if (verifGeneral[i] == 0) {
				jsonSortie = jsonSortie + "L'activité " + description[i].toLowerCase() +  " est dans une catégorie non reconnue. Elle sera ignorée. \n" ;
				f++ ;
			}
		}
		if (f > 0 ) {
			verificationCategorie = false ;
		}
	}

	public static String[] getDescriptions (){
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites") ;
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		description = new String[tailleArray] ;
		for (int i = 0 ; i < tailleArray ; i++){
			json = jsonArray.getJSONObject(i) ;
			description[i] = json.getString("description") ;
		}
		return description;
	}

	public static int getHeureCyclePrec(){
		int heure ;
		heure = (int) jsonObject.get("heures_transferees_du_cycle_precedent");
		return heure ;
	}

	public static void verifierHeuresSupp(int heure){
		verification++;
		jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
		if (heure < 0) {
			jsonSortie = jsonSortie + "Erreur, une heure ne peut pas être négative ou égal à 0 . \n" ;
			//Ecrire 0 dans JSON
		} else if (heure > 7) {
			//Ecrire 7 dans JSON
			jsonSortie = jsonSortie + " " + MSG_ERR_NOMBRE_HEURES_SUPP ;
			verificationHeureSupp = false ;
		} else {
			jsonSortie = jsonSortie + "Nombres d'heures valides \n" ;
		}
	}

	public static int getCumul(){
		int cumul = 0 ;
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites");
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		for (int i = 0 ; i < tailleArray ; i++){
			json = jsonArray.getJSONObject(i) ;
			cumul = cumul + json.getInt("heures") ;
		}
		return cumul ;
	}

	public static void verifierCumul(int cumul){
		verification++;
		jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
		if (cumul < 40) {
			jsonSortie = jsonSortie + "Il manque " + (40-cumul) + " heures de formation pour compléter le cycle. \n" ;
			verificationCumul = false ;
		} else {
			jsonSortie = jsonSortie + "Cumul total valide" ;
		}
	}

	public static int getCumulParCategorie(String[] categorieACumuler) {
		int cumul = 0 ;
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites");
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		int[] tabCumul = new int[tailleArray] ;
		for (int i = 0 ; i < tabCumul.length ; i++){
			tabCumul[i] = 0 ;
		}
		//for (int i = 0 ; i < tailleArray ; i++){
		//json = jsonArray.getJSONObject(i);
		for (int y = 0 ; y < categories.length ; y++) {
			for (int k = 0 ; k < categorieACumuler.length ; k++){
				if (categories[y].contains(categorieACumuler[k])) {
					tabCumul[y] = 1 ;
				} 
			}
		}
		//}

		for (int i = 0 ; i < tabCumul.length ; i++){
			json = jsonArray.getJSONObject(i) ;
			if (tabCumul[i] == 1) {
				cumul = cumul + json.getInt("heures") ;
			}
		}

		return cumul ;
	}

	public static void verifierCumulParCategorie (int cumulParCategorie) {
		verification++;
		jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
		if (cumulParCategorie < 17) {
			jsonSortie = jsonSortie + "Le cumul des catégories cours, atelier, séminaire, colloque, conférence, lecture dirigée ne suffit pas \n" ;
			verificationCumulCategorie = false ;
		} else {
			jsonSortie = jsonSortie + "Cumul par catégories valide \n" ;
		}
	}

	public static int getHeureActivite (String categorie){
		int heure = 0 ;
		jsonArray = (JSONArray) jsonObject.getJSONArray("activites") ;
		JSONObject json ;
		int tailleArray = jsonArray.size() ;
		for (int i = 0 ; i < tailleArray ; i++){
			json = jsonArray.getJSONObject(i) ;
			if (json.getString("categorie").equals(categorie)){
				heure = heure + json.getInt("heures") ;
			}
		}
		return heure ;
	}

	public static void verifierHeureActivite (String categorie) {
		switch (categorie)
		{
		case "présentation" :
			verification++;
			jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
			if (nbHeuresPresentation > 23) {
				//Ecrire 23 dans json
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " supérieur à 23 \n" ;
				verificationHeureActivite = false ;
			} else {
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " valide \n" ;
			}
			break;     
		case "groupe de discussion" :
			verification++;
			jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
			if (nbHeuresGroupeDisc > 17) {
				//Ecrire 17 dans json
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " supérieur à 17 \n" ;
				verificationHeureActivite = false ;
			} else {
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " valide \n" ;
			}
			break;    	
		case "projet de recherche" :
			verification++;
			jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
			if (nbHeuresProjetRecher > 23) {
				//Ecrire 23 dans json
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " supérieur à 23 \n" ;
				verificationHeureActivite = false ;
			} else {
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " valide \n" ;
			}
			break;     
		case "redaction" :
			verification++;
			jsonSortie = jsonSortie + "Verification " + (verification) + " : " ;
			if (nbHeuresRedaction > 17) {
				//Ecrire 17 dans json
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " supérieur à 17 \n" ;
				verificationHeureActivite = false ;
			} else {
				jsonSortie = jsonSortie + "Nombre d'heures de la catégorie " + categorie + " valide \n" ;
			}
			break;   
		default:
			
		}
	}

	public static boolean verificationCategorie (String[] categoriesValides , String categorie) {
		boolean test = false ;
		for (int k = 0 ; k < categoriesValides.length ; k++){
			if (categorie.contains(categoriesValides[k])) {
				test = true ;
			} 
		}
		return test ;
	}
	
	public static boolean genererJSON() {
		JSONObject resultat = new JSONObject() ;
			resultat.accumulate("complet", informationValide);
		JSONArray erreursArray = new JSONArray() ;
			
		JSONObject newRacine = new JSONObject() ;
		
		
		//erreurs.accumulate("erreurs", erreurs) ;
		
		try {
			Utf8File.saveStringIntoFile("", newRacine.toString(3));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true ;
	}
	
	public static void verificationfinale() {
		if (verificationCycle == false || verificationDate == false || verificationHeure == false || verificationCategorie == false ||
			verificationHeureSupp == false || verificationCumul == false || verificationCumulCategorie == false || verificationHeureActivite == false ) {
			informationValide = false ;
		}
	}
	
	public static String replaceCharAt(String s, int pos, char c) {
		return s.substring(0,pos) + c + s.substring(pos+1);
	}

}


