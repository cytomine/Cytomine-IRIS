/**
 * Created by lrollus on 7/14/14.
 */
angular.module("irisApp")
    .factory("cytomineService",function($http,projectUrl) {

        var publicKey;
        var privateKey;

        return {

        	// returns the public key
            getPublicKey : function() {
                return publicKey;
            },
            
            // returns the private key
            getPrivateKey : function() {
                return privateKey;
            },

            // sets the keys
            setKeys : function(publicK,privateK) {
                console.log("set key="+publicK + " " + privateK);
                publicKey = publicK;
                privateKey = privateK;
            },

            // add the keys to the end of an existing URL and return 
            // the concatenated URL
            addKeys : function(url) {
                url = url + ((url.indexOf("?")==-1)? "?" : "&");
                url = url + "publicKey={publicKey}&privateKey={privateKey}";
                url = url.replace("{publicKey}",this.getPublicKey());
                url = url.replace("{privateKey}",this.getPrivateKey());
                return url;
            }
        };
    });
