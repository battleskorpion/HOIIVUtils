# CreatePortableApp.ps1


# Copies this script from other this and freinkensteining it because I'm lazy


# WARNING DO NOT RUN THIS UNLESS YOUR ME!
# This is a homemade script to copy specific files, config and custom, from team fortress 2 game folder to the git(hub) repo.
# This is just for me as an easy auto of a particulary not obnoxious but not quick task that could be done manual in less time it took to write this script.
# FlAW 1: This will only copy files by the name, if I rename, delete, or create files this will become OUTDATED IMIDIATLY!
# FLAW 2: Only works on windows, specificly the windows main drive "C:\". THIS WON'T WORK IF YOU TF2 IS INSTALLED ON A DIFFERENT DRIVE!
# FLAW 3: Will only copy to expected folder ".\Documents\GitHub\ChrissCustomTF2Config"

# Stops the script if it runs into any issue
$ErrorActionPreference = "Stop"

#Make sure the folder exists
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils"))
{
    mkdir "$HOME\Desktop\HOIIVUtils"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\target"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\target"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\common"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\common"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\common\ideas"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\common\ideas"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\common\national_focus"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\common\national_focus"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\common\units"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\common\units"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\history"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\history"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\history\states"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\history\states"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\localisation"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\localisation"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\localisation\english"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\localisation\english"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\map"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\map"
}
if (-not (Test-Path "$HOME\Desktop\HOIIVUtils\demo_mod\map\strategicregions"))
{
    mkdir "$HOME\Desktop\HOIIVUtils\demo_mod\map\strategicregions"
}

Copy-Item -Path "$HOME\Documents\Github\HOIIVUtils\target\HOIIVUtils.jar" -Destination "$HOME\Desktop\HOIIVUtils\target\HOIIVUtils.jar" -Force
Copy-Item -Path "$HOME\Documents\Github\HOIIVUtils\HOIIVUtils.bat" -Destination "$HOME\Desktop\HOIIVUtils\HOIIVUtils.bat" -Force
Copy-Item -Path "$HOME\Documents\Github\HOIIVUtils\HOIIVUtils.sh" -Destination "$HOME\Desktop\HOIIVUtils\HOIIVUtils.sh" -Force



# # Username (yes I know there is a way to get this with env but I'm lazy)
# $CopyGameToGithubUser = $HOME
#
# # Set true to copy cfg files from tf2 to github
# $cfgtf2togithub = $true
#
# # Set true to copy custom files from tf2 to github
# $customtf2togithub = $true
#
#
#
# # Set true to copy cfg files from github to tf2
# $cfggithubtocfg = $false
#
# # Set true to copy custom files from github to tf2
# $customgithubtocfg = $false
#
# if ( $cfgtf2togithub -eq $cfggithubtocfg )
# {
# 	Write-Output "STOPPED! Don't copy loop cfg"
# 	exit
# }
# if ( $customtf2togithub -eq $customgithubtocfg )
# {
# 	Write-Output "STOPPED! Don't copy loop custom"
# 	exit
# }
#
#
# if ( $cfgtf2togithub )
# {
# 	Write-Output "Copying Files cfg tf2 to github"
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\autoexec.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\autoexec.cfg"						-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomAdvanceSettings.cfg"		 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomAdvanceSettings.cfg"			-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomCloseCaptionSettings.cfg"	 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomCloseCaptionSettings.cfg"		-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomGeneralBindsSettings.cfg"	 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomGeneralBindsSettings.cfg"		-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomMVMSettings.cfg"			 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomMVMSettings.cfg"				-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomNetworkingSettings.cfg"		 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomNetworkingSettings.cfg"		-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomNoTutorialSettings.cfg"		 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomNoTutorialSettings.cfg"		-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomNullMovementSettings.cfg"	 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomNullMovementSettings.cfg"		-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomShounicSpraysSettings.cfg"	 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomShounicSpraysSettings.cfg"	-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\scout.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\scout.cfg"							-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\soldier.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\soldier.cfg"						-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\pyro.cfg"							 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\pyro.cfg"							-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\demoman.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\demoman.cfg"						-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\heavyweapons.cfg"					 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\heavyweapons.cfg"					-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\engineer.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\engineer.cfg"						-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\medic.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\medic.cfg"							-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\sniper.cfg"						 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\sniper.cfg"							-Force
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\spy.cfg"							 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\spy.cfg"							-Force
#     Write-Output "Copied Files cfg tf2 to github"
# }
# if ( $customtf2togithub )
# {
# 	Write-Output "Copying Files custom tf2 to github"
# 	Copy-Item -Path "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\custom\*"				 -Destination "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\custom\"	 -Recurse -Force
#     Write-Output "Copied Files custom tf2 to github"
# }
# if ( $customgithubtocfg )
# {
# 	Write-Output "Copying Files custom github to tf2"
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\custom\*"		 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\custom\"			 -Recurse -Force
#     Write-Output "Copied Files custom github to tf2"
# }
# if ( $cfggithubtocfg )
# {
# 	Write-Output "Copying Files cfg github to tf2"
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\autoexec.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\autoexec.cfg"						-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomAdvanceSettings.cfg"		 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomAdvanceSettings.cfg"			-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomCloseCaptionSettings.cfg"	 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomCloseCaptionSettings.cfg"		-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomGeneralBindsSettings.cfg"	 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomGeneralBindsSettings.cfg"		-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomMVMSettings.cfg"			 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomMVMSettings.cfg"				-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomNetworkingSettings.cfg"		 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomNetworkingSettings.cfg"		-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomNoTutorialSettings.cfg"		 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomNoTutorialSettings.cfg"		-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomNullMovementSettings.cfg"	 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomNullMovementSettings.cfg"		-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\CustomShounicSpraysSettings.cfg"	 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\CustomShounicSpraysSettings.cfg"	-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\scout.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\scout.cfg"							-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\soldier.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\soldier.cfg"						-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\pyro.cfg"							 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\pyro.cfg"							-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\demoman.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\demoman.cfg"						-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\heavyweapons.cfg"					 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\heavyweapons.cfg"					-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\engineer.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\engineer.cfg"						-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\medic.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\medic.cfg"							-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\sniper.cfg"						 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\sniper.cfg"							-Force
# 	Copy-Item -Path "$CopyGameToGithubUser\Documents\GitHub\ChrissCustomTF2Config\tf\cfg\spy.cfg"							 -Destination "C:\Program Files (x86)\Steam\steamapps\common\Team Fortress 2\tf\cfg\spy.cfg"							-Force
#     Write-Output "Copied Files cfg github to tf2"
# }

Write-Output "DONE!"
