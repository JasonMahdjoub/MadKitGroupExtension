<?php
// Remplacement de glob qui est parfois bloqué chez les hébergeurs (free.fr par exemple)
// définition des contantes utiles à glob si elles ne le sont pas
foreach(array('GLOB_MARK'     => 2,
              'GLOB_NOSORT'   => 4,
              'GLOB_NOCHECK'  => 16,
              'GLOB_NOESCAPE' => 64,
              'GLOB_BRACE'    => 1024,
              'GLOB_ONLYDIR'  => 8192,
              'GLOB_ERR'      => 1) as $constant => $value) {
                  if (!defined($constant)) define($constant, $value) ;
                  // else echo "$constant : " . constant($constant) . "\n" ;
}

// définition de la fonction de remplacement de glob
// diffère légèrement de glob sur l'ordre des résultats si GLOB_BRACE est utilisé
function safe_glob($glob_pattern, $flags=0) {
    $pattern = basename($glob_pattern) ;
    $path    = dirname ($glob_pattern) ;
    if ($flags & GLOB_BRACE) {
        $brace   = ereg_replace("(.*){(.*)}(.*)", "(\\2)", $pattern) ;
        $brace   = strtr($brace, ',', '|') ;
        $pattern = ereg_replace("({.*})", $brace, $pattern) ;
    }
    $pattern = "^" . strtr($pattern, array('.'=>'\.', '*'=>'.*')) . "$" ;
    if (($dir = opendir($path)) !== false) {
        $files = array() ;
        while(($file = readdir($dir)) !== false) {
            if (ereg($pattern, $file) && !ereg("^\.", $file)) {
                if (($is_dir = is_dir("$path/$file")) || (!($flags & GLOB_ONLYDIR))) {
                    if ($is_dir && $flags & GLOB_MARK) {
                        $file .= '/' ;
                    }
                    $files[] = $path != '.' ? "$path/$file" : $file ;
                }
            }
        }
        closedir($dir) ;
        if (!($flags & GLOB_NOSORT)) {
            sort($files) ;
        }
        return $files ;
    }
    else {
        return false ;
    }
}
