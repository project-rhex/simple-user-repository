
import java.io.File;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.crypto.prng.VMPCRandomGenerator;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.nhindirect.stagent.CryptoExtensions;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.Map;

class DirectUserCertCreate
{
	private static final String PBE_WITH_MD5_AND_DES_CBC_OID  = "1.2.840.113549.1.5.3";

	
    static
    {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }	
	

    public static void main(String args[]) {
        String anchorFileName;
        String anchorPrivKeyFileName;
        char[] anchorPassword;
        String userName;
        String userEmail;
        
        if (args.length != 5) {
            System.out.println("ERROR: missing args: anchorFileName anchorPrivKeyFileName anchorPassword username useremail");
            System.exit(1);
        }
        anchorFileName = args[0];
        anchorPrivKeyFileName = args[1];
        anchorPassword = args[2].toCharArray();
        userName = args[3];
        userEmail = args[4];

        System.out.println("dbg: anchorFileName:" + anchorFileName + " anchorPrivKeyFileName:"+ anchorPrivKeyFileName + " anchorPassword:" + anchorPassword + " username:" + userName + " useremail:" + userEmail);


        System.out.print("Email validate ... ");
        // make sure the email is a valid email address
        try	{
            new InternetAddress(userEmail, true);
        }
        catch (AddressException e)	{
            System.out.println("The email address is an invalid address.");
			System.exit(1);
		}	
        System.out.println("success");

        // read anchor file, parse out needed fields, 
        File certFile = new File(anchorFileName);
		File keyFile = new File(anchorPrivKeyFileName);
		
		if (!certFile.exists())	{
			System.out.println("Certificate file does not exist or cannot be found.");
			System.exit(1);
		}
		
		if (!keyFile.exists())		{
			System.out.println("Private key file does not exist or cannot be found.");
			System.exit(1);
		}
		
		// load the certs from the file system
        System.out.print("Loading certs from file ... ");
		CertCreateFields signerFullCert = null;
		try		{
			signerFullCert = CertLoader.loadCertificate(certFile, keyFile, anchorPassword);
		}
		catch (Exception e)		{
			System.out.println("An error occured loading the certificate authority: " + e.getMessage());
		    System.exit(1);
		}

        if (signerFullCert == null)		{
			System.out.println("An error occured loading the certificate the authority: unknown error");
		    System.exit(1);
		}


        X509Certificate signerCert = signerFullCert.getSignerCert();
        PrivateKey signerKey = (PrivateKey)signerFullCert.getSignerKey();
        //** expire days
        int exp = 365;
        //** key strength
        int keyStre =  1024;

        System.out.println("Anchor and key cert loaded success");
        System.out.println("** DEBUG signer CERT: ");
        signerFullCert.dump();

        System.exit(1);

        //*******************
        System.out.print("Building user cert fields ... ");
        Map<String, Object> attributes = new HashMap<String, Object>(); 
        attributes.put("EMAILADDRESS", userEmail);	
        //** CN field -- is username
        System.out.print("userNAME: ..." + userName + "...");
        attributes.put("CN", userName);        
        String country;
        if (signerFullCert.getAttributes().containsKey("C"))
            country = new String(signerFullCert.getAttributes().get("C").toString());
        else
            country = "";
        attributes.put("C", country);

        String state;
        if (signerFullCert.getAttributes().containsKey("ST"))
            state = new String(signerFullCert.getAttributes().get("ST").toString());
        else
            state = "";
        attributes.put("ST", state);

        String local;
        if (signerFullCert.getAttributes().containsKey("L"))
            local = new String(signerFullCert.getAttributes().get("L").toString());
        else
            local = "";
        attributes.put("L", local);

        String org;
        if (signerFullCert.getAttributes().containsKey("O"))
            org = new String(signerFullCert.getAttributes().get("O").toString());
        else
            org = "";
        attributes.put("O", org);

        //*******************
        // create the new files
        File userCertFile = createNewFileName(userEmail, false);					
        File userKeyFile  = createNewFileName(userEmail, true);	
        if (userCertFile == null || userKeyFile == null)  {
			System.out.println("error on userCert file creation");
			System.exit(1);
		}

        if (userCertFile.exists() || userKeyFile.exists())  {
			System.out.println("The certificate or key file already exists for this email address.");
			System.exit(1);
		}


        //** empty password string for USER cert
        char[] userCertPassword = new String("").toCharArray();
        CertCreateFields userCertFields = new CertCreateFields(attributes, userCertFile, userKeyFile,
                                                               userCertPassword, exp, keyStre, signerCert, signerKey);

        System.out.println("success!");
        //System.out.println("** DEBUG USER cert: ");
        // userCertFields.dump();

        //*******************
        System.out.println("Creating user cert ... ");
        // create the cert
        CertCreateFields userCert = null;
        try	{
            userCert = CertGenerator.createCertificate(userCertFields, false);
        }
        catch (Exception e) {
            System.out.println("An error occured creating the certificate: " + e.getMessage());
		    System.exit(1);		
        }
        
        if (userCert == null) {
            System.out.println("An error occured creating the certificate: unknown error");
		    System.exit(1);	
        }
        System.out.println("success!");

        System.out.println("Creating PKCS12 file ... ");
        //public static File           create(File certFile, File keyFile, String password, File createFile)
        File pcks12File = CreatePKCS12.create(userCert.getNewCertFile(), userCert.getNewKeyFile(), new String(userCertPassword), null);
        
        if (pcks12File == null) {
            System.out.println("An error occured creating the pkcs12 file: unknown error");
            System.exit(1);	
        }
        System.out.println("success!");
        

    } 


    //****
    private static File createNewFileName(String emailField, boolean isKey)
	{
		String fileName = null;
		
		int index;
		String field = emailField;
		if (field.isEmpty() == true)
            return null;

        index = field.indexOf("@");
        if (index > -1)
            fileName = field.substring(0, index);
        else
            fileName = field;
		
		if (isKey)
			fileName += "Key";
		
		fileName += ".der";
		
		return new File(fileName);
		
	}


        
    private static long generatePositiveRandom()        {
        Random ranGen;
        long retVal = -1;
        byte[] seed = new byte[8];
        VMPCRandomGenerator seedGen = new VMPCRandomGenerator();
        seedGen.addSeedMaterial(new SecureRandom().nextLong());
        seedGen.nextBytes(seed);
        ranGen = new SecureRandom(seed);
        while (retVal < 1)  { 
            retVal = ranGen.nextLong();                                             
        }
        
        return retVal;
    }

    /*	private static CertCreateFields createLeafCertificate(CertCreateFields fields, KeyPair keyPair, boolean addAltNames) throws Exception
	{
		String altName = "";
		StringBuilder dnBuilder = new StringBuilder();
		
		// create the DN
		if (fields.getAttributes().containsKey("EMAILADDRESS"))
		{
			dnBuilder.append("EMAILADDRESS=").append(fields.getAttributes().get("EMAILADDRESS")).append(", ");
			altName = fields.getAttributes().get("EMAILADDRESS").toString();
		}
		
		if (fields.getAttributes().containsKey("CN"))
			dnBuilder.append("CN=").append(fields.getAttributes().get("CN")).append(", ");
		
		if (fields.getAttributes().containsKey("C"))
			dnBuilder.append("C=").append(fields.getAttributes().get("C")).append(", ");
		
		if (fields.getAttributes().containsKey("ST"))
			dnBuilder.append("ST=").append(fields.getAttributes().get("ST")).append(", ");	
		
		if (fields.getAttributes().containsKey("L"))
			dnBuilder.append("L=").append(fields.getAttributes().get("L")).append(", ");	
		
		if (fields.getAttributes().containsKey("O"))
			dnBuilder.append("O=").append(fields.getAttributes().get("O")).append(", ");				
		
		String DN = dnBuilder.toString().trim();
		if (DN.endsWith(","))
			DN = DN.substring(0, DN.length() - 1);
		
		X509V3CertificateGenerator  v1CertGen = new X509V3CertificateGenerator();
		
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		end.add(Calendar.DAY_OF_MONTH, fields.getExpDays()); 
		
        v1CertGen.setSerialNumber(BigInteger.valueOf(generatePositiveRandom())); // not the best way to do this... generally done with a db file
        v1CertGen.setIssuerDN(fields.getSignerCert().getSubjectX500Principal()); // issuer is the parent cert
        v1CertGen.setNotBefore(start.getTime());
        v1CertGen.setNotAfter(end.getTime());
        v1CertGen.setSubjectDN(new X509Principal(DN));
        v1CertGen.setPublicKey(keyPair.getPublic());
        v1CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
        
		// pointer to the parent CA
        v1CertGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
        		new AuthorityKeyIdentifierStructure(fields.getSignerCert()));

        v1CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(keyPair.getPublic()));

        boolean allowToSign = (fields.getAttributes().get("ALLOWTOSIGN") != null && 
        		fields.getAttributes().get("ALLOWTOSIGN").toString().equalsIgnoreCase("true"));
        
        v1CertGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(allowToSign));
        
        if (fields.getSignerCert().getSubjectAlternativeNames() != null)
        {
        	for (List<?> names : fields.getSignerCert().getSubjectAlternativeNames())
        	{
        		GeneralNames issuerAltName = new GeneralNames(new GeneralName((Integer)names.get(0), names.get(1).toString()));
        	
        		v1CertGen.addExtension(X509Extensions.IssuerAlternativeName, false, issuerAltName);
        	}
        }
        
        if (addAltNames && !altName.isEmpty())
        {        	
        	int nameType = altName.contains("@") ? GeneralName.rfc822Name : GeneralName.dNSName;
        	
        	GeneralNames subjectAltName = new GeneralNames(new GeneralName(nameType, altName));
        	
            v1CertGen.addExtension(X509Extensions.SubjectAlternativeName, false, subjectAltName);

        }        
        
        // use the CA's private key to sign the certificate
        X509Certificate newCACert = v1CertGen.generate((PrivateKey)fields.getSignerKey(), CryptoExtensions.getJCEProviderName());
        
        // validate the certificate 
        newCACert.verify(fields.getSignerCert().getPublicKey());
        
        // write the certificate the file system
        writeCertAndKey(newCACert, keyPair.getPrivate(), fields);
       
        return fields;
	}	
	
	private static void writeCertAndKey(X509Certificate cert, PrivateKey key, CertCreateFields fields) throws Exception
	{
		// write the cert
		FileUtils.writeByteArrayToFile(fields.getNewCertFile(), cert.getEncoded());		
		
		if (fields.getNewPassword() == null || fields.getNewPassword().length == 0)
		{
			// no password... just write the file 
			FileUtils.writeByteArrayToFile(fields.getNewKeyFile(), key.getEncoded());
		}
		else
		{
			// encypt it, then write it
			
			// prime the salts
			byte[] salt = new byte[8];
			VMPCRandomGenerator ranGen = new VMPCRandomGenerator();
			ranGen.addSeedMaterial(new SecureRandom().nextLong());
			ranGen.nextBytes(salt);

			// create PBE parameters from salt and iteration count
			PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, 20);
			   

			PBEKeySpec pbeKeySpec = new PBEKeySpec(fields.getNewPassword());
			SecretKey sKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES", CryptoExtensions.getJCEProviderName()).generateSecret(pbeKeySpec); 
			
			// encrypt
			Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES", CryptoExtensions.getJCEProviderName());
			cipher.init(Cipher.ENCRYPT_MODE, sKey, pbeSpec, null);
			byte[] plain = (byte[])key.getEncoded();
			byte[] encrKey = cipher.doFinal(plain, 0, plain.length);

			// set the algorithm parameters
			AlgorithmParameters pbeParams = AlgorithmParameters.getInstance(PBE_WITH_MD5_AND_DES_CBC_OID, Security.getProvider("SunJCE"));

			pbeParams.init(pbeSpec);

			// place in a EncryptedPrivateKeyInfo to encode to the proper file format
			EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(pbeParams,encrKey);
			
			// now write it to the file
			FileUtils.writeByteArrayToFile(fields.getNewKeyFile(), info.getEncoded());
		}
			
		if (fields.getSignerCert() == null)
			fields.setSignerCert(cert);
		
		if (fields.getSignerKey() == null)
			fields.setSignerKey(key);
	}
    */

}