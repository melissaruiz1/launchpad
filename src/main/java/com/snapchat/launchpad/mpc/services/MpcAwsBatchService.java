package com.snapchat.launchpad.mpc.services;


import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.AssignPublicIp;
import com.amazonaws.services.batch.model.ContainerOverrides;
import com.amazonaws.services.batch.model.ContainerProperties;
import com.amazonaws.services.batch.model.EFSVolumeConfiguration;
import com.amazonaws.services.batch.model.FargatePlatformConfiguration;
import com.amazonaws.services.batch.model.JobDefinitionType;
import com.amazonaws.services.batch.model.KeyValuePair;
import com.amazonaws.services.batch.model.MountPoint;
import com.amazonaws.services.batch.model.NetworkConfiguration;
import com.amazonaws.services.batch.model.PlatformCapability;
import com.amazonaws.services.batch.model.RegisterJobDefinitionRequest;
import com.amazonaws.services.batch.model.RegisterJobDefinitionResult;
import com.amazonaws.services.batch.model.ResourceRequirement;
import com.amazonaws.services.batch.model.ResourceType;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.Volume;
import com.snapchat.launchpad.common.configs.AwsBatchConfig;
import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("batch-aws")
@Service
public class MpcAwsBatchService extends MpcBatchService {
    private final AWSBatch awsBatch =
            AWSBatchClientBuilder.standard().withRegion("us-east-1").build();

    @Autowired private AwsBatchConfig awsBatchConfig;

    @Override
    public String submitBatchJob(MpcJobDefinition jobDef) throws NoSuchElementException {
        String jobDefId = "mpc-" + UUID.randomUUID();

        NetworkConfiguration networkConfiguration =
                new NetworkConfiguration().withAssignPublicIp(AssignPublicIp.ENABLED);
        ResourceRequirement cpuResourceRequirement =
                new ResourceRequirement().withType(ResourceType.VCPU).withValue("4");
        ResourceRequirement memoryResourceRequirement =
                new ResourceRequirement().withType(ResourceType.MEMORY).withValue("8192");
        EFSVolumeConfiguration efsVolumeConfiguration =
                new EFSVolumeConfiguration().withFileSystemId(awsBatchConfig.getVolume());
        Volume volume =
                new Volume()
                        .withName(efsVolumeConfiguration.getFileSystemId())
                        .withEfsVolumeConfiguration(efsVolumeConfiguration);
        MountPoint mountPoint =
                new MountPoint()
                        .withSourceVolume(volume.getEfsVolumeConfiguration().getFileSystemId())
                        .withContainerPath(STORAGE_PATH);
        FargatePlatformConfiguration fargatePlatformConfiguration =
                new FargatePlatformConfiguration().withPlatformVersion("1.4.0");
        ContainerProperties containerProperties =
                new ContainerProperties()
                        .withFargatePlatformConfiguration(fargatePlatformConfiguration)
                        .withExecutionRoleArn(awsBatchConfig.getExecutionRoleArn())
                        .withJobRoleArn(awsBatchConfig.getJobRoleArn())
                        .withNetworkConfiguration(networkConfiguration)
                        .withResourceRequirements(cpuResourceRequirement, memoryResourceRequirement)
                        .withVolumes(volume)
                        .withImage(jobDef.getImage())
                        .withEnvironment(
                                new KeyValuePair().withName("STORAGE_PATH").withValue(STORAGE_PATH))
                        .withCommand("/bin/bash", "-c", jobDef.getCommand())
                        .withMountPoints(mountPoint);
        RegisterJobDefinitionRequest registerJobDefinitionRequest =
                new RegisterJobDefinitionRequest()
                        .withJobDefinitionName(jobDefId)
                        .withPlatformCapabilities(PlatformCapability.FARGATE)
                        .withType(JobDefinitionType.Container)
                        .withContainerProperties(containerProperties);
        RegisterJobDefinitionResult jobDefinitionResult =
                awsBatch.registerJobDefinition(registerJobDefinitionRequest);

        String jobId = jobDefinitionResult.getJobDefinitionName();
        ContainerOverrides containerOverrides =
                new ContainerOverrides()
                        .withEnvironment(
                                new KeyValuePair()
                                        .withName("COMPANY_IP")
                                        .withValue(jobDef.getCompanyIp()));
        SubmitJobRequest request =
                new SubmitJobRequest()
                        .withJobName(jobId)
                        .withJobQueue(awsBatchConfig.getJobQueue())
                        .withJobDefinition(jobDefinitionResult.getJobDefinitionArn())
                        .withContainerOverrides(containerOverrides);
        return awsBatch.submitJob(request).toString();
    }
}
