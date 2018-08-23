FROM jenkins/jenkins:lts

USER root
RUN mkdir /usr/share/jenkins/ref/plugins && \
    chown -R jenkins:jenkins /usr/share/jenkins/ref/plugins

# Add plugin dependencies
COPY --chown=jenkins:jenkins docs/examples/jenkins/files/jenkins_home/plugins.txt /etc/files/plugins.txt

USER jenkins
RUN /usr/local/bin/install-plugins.sh < /etc/files/plugins.txt

# Install Clerk plugin
COPY --chown=jenkins:jenkins jenkins-plugin/build/libs/build-clerk.hpi /usr/share/jenkins/ref/plugins/build-clerk.hpi

# Init scripts
COPY --chown=jenkins:jenkins docs/examples/jenkins/files/jenkins_home/init.groovy.d/* /usr/share/jenkins/ref/init.groovy.d/

# Copy sample config
COPY --chown=jenkins:jenkins docs/examples/jenkins/files/jenkins_home/jobs /var/jenkins_home/jobs
COPY --chown=jenkins:jenkins docs/examples/jenkins/files/jenkins_home /var/jenkins_home

USER jenkins
