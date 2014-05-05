require 'berkshelf/vagrant'

Vagrant.configure("2") do |config|
  
  config.vm.box = "hashicorp/precise64"
  config.vm.network :forwarded_port, guest: 27017, host: 27017
  config.vm.network :forwarded_port, guest: 9200, host: 9200
  config.vm.network :forwarded_port, guest: 9300, host: 9300
  config.berkshelf.enabled = true
  
  # Install latest Chef on the machine
  config.vm.provision :shell do |shell|
    shell.inline = %Q{
      R=`which ruby` || R='/opt/vagrant_ruby/bin/ruby'

	  test -x $R && $R -e 'File.mtime("/var/lib/apt/lists/partial/") < Time.now - 3600 ? exit(1) : exit(0)' > /dev/null  2>&1 || \
	    (
	    apt-get update --quiet --yes && \
	    apt-get install curl vim --quiet --yes
	    )

	  test -d "/opt/chef" || \
	    (
	    curl -# -L http://www.opscode.com/chef/install.sh | bash
	    )
	}
  end

  config.vm.provision "chef_solo" do |chef|
  	chef.log_level = :debug
    chef.add_recipe "mongodb"
    chef.add_recipe "java"
    chef.add_recipe "elasticsearch"
  end

end