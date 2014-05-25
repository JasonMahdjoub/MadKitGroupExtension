<?php

require_once 'glob.include.php';

class VersionMadKit
{
	public $madkit_version;

	public function __construct($madkit_version)
	{
		$this->madkit_version=$madkit_version;
	}
	public function getJSInstance()
	{
		return "new VersionMadKit('".$this->madkit_version."')";
	}
	public function __toString()
	{
		return $this->madkit_version;
	}

}
class VersionType
{
	public $type_and_version;
	public $madkit_versions=array();

	public function __construct($type_and_version)
	{
		$this->type_and_version=$type_and_version;
	}
	public function add($madkit_version)
	{
		if (!array_key_exists($madkit_version, $this->madkit_versions))
		{
			$this->madkit_versions[$madkit_version]=new VersionMadKit($madkit_version);
		}
	}

	public function getJSInstance()
	{
		$js="new VersionType('".$this->type_and_version."', new Array(";

		$first=true;
		foreach($this->madkit_versions as $mkv)
		{
			if ($first)
				$first=false;
			else
				$js.=", ";
			$js.=$mkv->getJSInstance();
		}
		$js.="))";
		return $js;
	}

	public function __toString()
	{
		return $this->type_and_version;
	}
	public function doSort()
	{
		arsort($this->madkit_versions, SORT_REGULAR);
	}

}
class VersionRevision
{
	public $revision=0;
	public $types=array();

	public function __construct($revision)
	{
		$this->revision=$revision;
	}

	public function add($type, $madkit_version)
	{
		if (!array_key_exists($type, $this->types))
		{
			$this->types[$type]=new VersionType($type);
		}
		$this->types[$type]->add($madkit_version);
	}

	public function getJSInstance()
	{
		$js="new VersionRevision('".$this->revision."', new Array(";

		$first=true;
		foreach($this->types as $type)
		{
			if ($first)
				$first=false;
			else
				$js.=", ";
			$js.=$type->getJSInstance();
		}
		$js.="))";
		return $js;
	}
	public function __toString()
	{
		return "".$this->revision;
	}
	public function doSort()
	{
		/*$notok=true;
		while ($notok)
		{
			$notok=false;
			$previous=$this->types[0];
			for (int i=1;i<length($this->types);i++)
			{
				if (preg_match("/^alpha/", basename($this->types[$i-1]) && preg_match("/^beta/", basename($this->types[$i]))
				{
					$tmp=$this->types[$i-1];
					$this->types[$i-1]=$this->types[$i];
					$this->types[$i]=$tmp;
					$notok=true;
				}
				else
				{
					$pregc=null;
					$pregp=null;
					if (preg_match("/^alpha/", basename($this->types[$i-1]))
						$revp="alpha
					if (preg_match("/^alpha/", basename($this->types[$i-1]) && preg_match("/^beta/", basename($this->types[$i]))
					preg_match("/^alpha/", basename($this->types[$i-1])
				}
			}
			if (preg_match("/^alpha-/", basename($dir)))
		}*/
		arsort($this->types, SORT_REGULAR);
		//natsort($this->types);
		foreach($this->types as $type)
		{
			$type->doSort();
		}
	}

}

class VersionMinor
{
	public $minor=0;
	public $revisions=array();

	public function __construct($minor)
	{
		$this->minor=$minor;
	}
	public function add($revision, $type, $madkit_version)
	{
		if (!array_key_exists($revision, $this->revisions))
		{
			$this->revisions[$revision]=new VersionRevision($revision);
		}
		$this->revisions[$revision]->add($type, $madkit_version);

	}
	public function getJSInstance()
	{
		$js="new VersionMinor('".$this->minor."', new Array(";

		$first=true;
		foreach($this->revisions as $revision)
		{
			if ($first)
				$first=false;
			else
				$js.=", ";
			$js.=$revision->getJSInstance();
		}
		$js.="))";
		return $js;
	}
	public function __toString()
	{
		return "".$this->minor;
	}
	public function doSort()
	{
		arsort($this->revisions, SORT_REGULAR);
		foreach($this->revisions as $revision)
		{
			$revision->doSort();
		}
	}


}

class VersionMajor
{
	public $major=0;
	
	public $minors=array();
	
	public function __construct($major)
	{
		$this->major=$major;
	}

	public function add($minor, $revision, $type, $madkit_version)
	{
		if (!array_key_exists($minor, $this->minors))
		{
			$this->minors[$minor]=new VersionMinor($minor);
		}
		$this->minors[$minor]->add($revision, $type, $madkit_version);
	}

	public function getJSInstance()
	{
		$js="new VersionMajor('".$this->major."', new Array(";

		$first=true;
		foreach($this->minors as $minor)
		{
			if ($first)
				$first=false;
			else
				$js.=", ";
			$js.=$minor->getJSInstance();
		}
		$js.="))";
		return $js;
	}
	public function __toString()
	{
		return "".$this->major;
	}
	public function doSort()
	{
		arsort($this->minors, SORT_REGULAR);
		foreach($this->minors as $minor)
		{
			$minor->doSort();
		}
	}


}

class Versions
{
	const downloads_dir="./downloads/";
	public $majors=array();

	public function __construct()
	{
	}

	public function addFile($file)
	{
		$tab=array();
		$file=basename($file);
		if (preg_match("/^mkge-([0-9]+)\.([0-9]+)\.([0-9]+)(Stable|(Alpha[0-9]+|Beta[0-9]+))\+madkit-([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)/", $file, $tab))
		{
			$major=$tab[1];
			if (!array_key_exists($major, $this->majors))
			{
				$this->majors[$major]=new VersionMajor($major);
			}
			$this->majors[$major]->add($tab[2], $tab[3], $tab[4], $tab[6]);
		}
		else
			throw new Exception("Invalid file: ".$file);
		
	}

	public function getJSInstance()
	{
		$js="new Versions(new Array(";
		$first=true;
		foreach ($this->majors as $major)
		{
			if ($first)
				$first=false;
			else
				$js.=", ";
			$js.=$major->getJSInstance();
		}
		$js.="))";
		return $js;
	}

	public function doSort()
	{
		arsort($this->majors, SORT_REGULAR);
		foreach($this->majors as $major)
		{
			$major->doSort();
		}
	}
	public static function getVersions()
	{
		$dirs=safe_glob(self::downloads_dir."*", GLOB_ONLYDIR);
		$versions=new Versions();
		$directories=array();
		if (!isset($dirs) || $dirs==null)
		{
			return $versions;
		}
		foreach($dirs as $dir)
		{
			if (preg_match("/^mkge-/", basename($dir)))
			{
				$directories[]=$dir;
				
			}
		}
	

		foreach ($directories as $dir)
		{
			$files=safe_glob($dir."/WithMadKit/*.jar");
			if (isset($files) && $files!=null)
			{
				foreach($files as $file)
				{
					$versions->addFile($file);
				}
			}
		}
		$versions->doSort();
		return $versions;
	}


}



$versions=Versions::getVersions();

echo <<<HTML
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>MadKitGroupExtension downloads - Choose your version</title>
		<script type="text/javascript" language="Javascript">
HTML;


echo <<<HTML
		</script>
	</head>
		
	<body>
HTML;
	if (count($versions->majors)==0)
	{
		echo <<<HTML
<h2>There is no version of MadKitGroupExtension to download on this server !</h2>
HTML;
	}
	else
	{
		echo <<<HTML
		<h1>MadKitGroupExtension downloads</h1>
		The default parameters enable to get the latest version.<br><br>
		In order to reduce problems of versions betwen MadKitGroupExtension and MadKit, it is recommended to download an archive with madkit included (default option). <br><br>

		<table style='border-color:transparent;border-spacing:10px;'>
			<tr><td rowspan=2>Select your version (from Major to Type)<td style='text-align:center;'>Major<td style='text-align:center;'>Minor<td style='text-align:center;'>Revision<td style='text-align:center;'>Type
			<tr>		  
				<td style='text-align:center;'>
					<select name="major" id="major" onChange='majorChanged()'>
						<option value='-1'>Loading</option>
					</select>
				<td style='text-align:center;'>
					<select name="minor" id="minor" onChange='minorChanged()'>
						<option value='-1'>Loading</option>
					</select>
				<td style='text-align:center;'>		
					<select name="revision" id="revision" onChange='revisionChanged()'>
						<option value='-1'>Loading</option>
					</select>
				<td style='text-align:center;'>
					<select name="type" id="type" onChange='typeChanged()'>
						<option value='-1'>Loading</option>
					</select>
			<tr> 
				<td>Select the madkit version you want to be included.<br>Select 'Do not include MadKit' else.
				<td colspan=4>
					<select name="madkit" id="madkit" onChange='madkitChanged()'>
						<option value='-1'>Loading</option>
					</select>
			<tr>
				<td><label for="src">Include source code</label><td colspan=5>
					<input type="checkbox" name="src" id="src" value="src" checked>
			<tr>
				<td><label for="doc">Include documentation</label><td colspan=5>
					<input type="checkbox" name="doc" id="doc" value="doc" checked>		
			<tr>
				<td colspan=5 style='text-align:center;'>	
				<button type="button" onClick="getFile()">Download</button>
		</table>
		<script type="text/javascript" language="Javascript">
HTML;
echo <<<JS
	function VersionMadKit(mk_version)
	{
		this.madkit_version=mk_version;
	}
	function VersionType(vtype, mk_versions)
	{
		this.version_type=vtype;
		this.madkit_versions=mk_versions;
	}
	function VersionRevision(vrevision, vtypes)
	{
		this.version_revision=vrevision;
		this.version_types=vtypes;
	}
	function VersionMinor(vminor, vrevisions)
	{
		this.version_minor=vminor;
		this.version_revisions=vrevisions;
	}
	function VersionMajor(vmajor, vminors)
	{
		this.version_major=vmajor;
		this.version_minors=vminors;
	}
	function Versions(vmajors)
	{
		this.version_majors=vmajors;
	}
	var currentmajor=null;
	var currentminor=null;
	var currentrevision=null;
	var currenttype=null;
	var currentmadkit=null;
	function setType(type)
	{
		currenttype=type;
		var mk=document.getElementById("madkit");
		mk.innerHTML="";
		var first=true;
		var op=document.createElement("option");
		op.text="Do not include MadKit";
		op.value="nomadkit";
		mk.add(op);
		for (var i=0;i<type.madkit_versions.length;i++)
		{
			var m=type.madkit_versions[i];
			var option=document.createElement("option");
			option.text=m.madkit_version;
			option.value=m.madkit_version;
			if (first)
			{
				first=false;
				option.selected=true;
				currentmadkit=m;
			}
			mk.add(option);
		}
	}
	function setRevision(revision)
	{
		currentrevision=revision;
		var type=document.getElementById("type");
		type.innerHTML="";
		var first=true;
		for (var i=0;i<revision.version_types.length;i++)
		{
			var t=revision.version_types[i];
			var option=document.createElement("option");
			option.text=t.version_type;
			option.value=t.version_type;
			if (first)
			{
				first=false;
				option.selected=true;
			}
			type.add(option);
		}
		setType(revision.version_types[0]);
	}	
	function setMinor(minor)
	{
		currentminor=minor;
		var revision=document.getElementById("revision");
		revision.innerHTML="";
		var first=true;
		for (var i=0;i<minor.version_revisions.length;i++)
		{
			var r=minor.version_revisions[i];
			var option=document.createElement("option");
			option.text=r.version_revision;
			option.value=r.version_revision;
			if (first)
			{
				first=false;
				option.selected=true;
			}
			revision.add(option);
		}
		setRevision(minor.version_revisions[0]);
	}
	function setMajor(major)
	{
		currentmajor=major;
		var minor=document.getElementById("minor");
		minor.innerHTML="";
		var first=true;
		for (var i=0;i<major.version_minors.length;i++)
		{
			var m=major.version_minors[i];
			var option=document.createElement("option");
			option.text=m.version_minor;
			option.value=m.version_minor;
			if (first)
			{
				first=false;
				option.selected=true;
			}
			minor.add(option);
		}
		setMinor(major.version_minors[0]);
	}
	function setMajors(versions)
	{
		var major=document.getElementById("major");
		major.innerHTML="";
		var first=true;
		for (var i=0;i<versions.version_majors.length;i++)
		{
			var m=versions.version_majors[i];
			var option=document.createElement("option");
			option.text=m.version_major;
			option.value=m.version_major;
			if (first)
			{
				first=false;
				option.selected=true;
			}
			major.add(option);
			var option=document.createElement("option");
		}
		setMajor(versions.version_majors[0]);
	}
	var versions={$versions->getJSInstance()};


	setMajors(versions);

	function majorChanged()
	{
		major=document.getElementById("major");
		option=major.options[major.selectedIndex];
		for (var i=0;i<versions.version_majors.length;i++)
		{
			var m=versions.version_majors[i];
			if (m.version_major==option.value)
			{
				setMajor(m);
				break;
			}
		}
	}
	function minorChanged()
	{
		minor=document.getElementById("minor");
		option=minor.options[minor.selectedIndex];
		for (var i=0;i<currentmajor.version_minors.length;i++)
		{
			var m=currentmajor.version_minors[i];
			if (m.version_minor==option.value)
			{
				setMinor(m);
				break;
			}
		}
	}
	function revisionChanged()
	{
		revision=document.getElementById("revision");
		option=revision.options[revision.selectedIndex];
		for (var i=0;i<currentminor.version_revisions.length;i++)
		{
			var r=currentminor.version_revisions[i];
			if (r.version_revision==option.value)
			{
				setRevision(r);
				break;
			}
		}
	}
	function typeChanged()
	{
		type=document.getElementById("type");
		option=type.options[type.selectedIndex];
		for (var i=0;i<currentrevision.version_types.length;i++)
		{
			var t=currentrevision.version_types[i];
			if (t.version_type==option.value)
			{
				setType(t);
				break;
			}
		}
	}
	function madkitChanged()
	{
		mk=document.getElementById("madkit");
		option=mk.options[mk.selectedIndex];
		if (mk.value=="nomadkit")
			currentmadkit=null;
		else
		{
			for (var i=0;i<currenttype.madkit_versions.length;i++)
			{
				var m=currenttype.madkit_versions[i];
				if (m.madkit_version==option.value)
				{
					currentmadkit=m;
					break;
				}
			}
		}
	}

	function getFile()
	{
		url="http://madkitgroupextension.free.fr/downloads/mkge-"+currentmajor.version_major+"."+currentminor.version_minor+"/";
		if (currentmadkit==null)
		{
			url=url+"WithoutMadKit/mkge-"+currentmajor.version_major+"."+currentminor.version_minor+"."+currentrevision.version_revision+currenttype.version_type;
			src=document.getElementById("src");
			if (src.checked)
			{
				url=url+"_withsrc";
			}
			doc=document.getElementById("doc");			
			if (doc.checked)
			{
				url=url+"_withdoc.zip";
			}
			else
			{
				url=url+".jar";
			}

		}
		else
		{
			url=url+"WithMadKit/mkge-"+currentmajor.version_major+"."+currentminor.version_minor+"."+currentrevision.version_revision+currenttype.version_type+"+madkit-"+currentmadkit.madkit_version;
			src=document.getElementById("src");
			if (src.checked)
			{
				url=url+"_withsrc";
			}
			doc=document.getElementById("doc");			
			if (doc.checked)
			{
				url=url+"_withdoc.zip";
			}
			else
			{
				url=url+".jar";
			}
		}
		setTimeout("window.location.href = '"+url+"'" , 0 );
		//location.reload(url);
	}
	
JS;
echo <<<HTML
		</script>
HTML;
}
echo <<<HTML
	</body>
</html>
HTML;

?>

