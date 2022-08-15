package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GuildSettings {
    public Long AdminChannel;
    public Long CmdChannel;
    public String OODAdditionalInfo;

    public Long getAdminChannel() {return AdminChannel;}
    public void setAdminChannel(Long adminChannel) {AdminChannel = adminChannel;}

    public Long getCmdChannel() {return CmdChannel;}
    public void setCmdChannel(Long cmdChannel) {CmdChannel = cmdChannel;}

    public String getOODAdditionalInfo() {return OODAdditionalInfo;}
    public void setOODAdditionalInfo(String oodAdditionalInfo) {OODAdditionalInfo = oodAdditionalInfo;}
}
