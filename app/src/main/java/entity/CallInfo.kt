package entity

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 * Created By 1524 on 10/1/2020
 */
data class CallInfo() : Parcelable {
    var callCount: String? = null
    var dealerCode: String? = null
    var noResponseScheduledCallDate: String? = null
    var noServiceReason: String? = null
    var followUpDate: String? = null
    var followUpTime: String? = null
    var pickUpAddress: String? = null
    var isFollowupRequired: String? = null
    var customerName: String? = null
    var currentAddress: String? = null
    var appointmentType: String? = null
    var permanentAddress: String? = null
    var officeAddress: String? = null
    var customerEmail: String? = null
    var customerPhone: String? = null
    var doa: String? = null
    var dob: String? = null
    var agentName: String? = null
    var callType: String? = null
    var freeService: String? = null
    var lastCallDate: String? = null
    var nextCallDate: String? = null
    var notes: String? = null
    var pickUpRequired: String? = null
    var serviceScheduled: String? = null
    var serviceScheduledTime: String? = null
    var serviceScheduledDate: String? = null
    var status: String? = null
    var appointmentDate: String? = null
    var defectDetails: String? = null
    var labourDetails: String? = null
    var lastServiceDate: String? = null
    var milege: String? = null
    var model: String? = null
    var nextServiceDue: String? = null
    var saleDate: String? = null
    var serviceAdvisor: String? = null
    var serviceType: String? = null
    var technician: String? = null
    var vehicleNumber: String? = null
    var lastBatteryChangeDate: String? = null
    var lastDrumBrakeChange: String? = null
    var lastOilChange: String? = null
    var vasNotes: String? = null
    var filePath: String? = null
    var callDate: String? = null
    var callTime: String? = null
    var callDuration: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var comments: String? = null
    var isCallMade: String? = null
    var mediaFile: String? = null
    var callTypePicId = 0
    var vehicalRegNo: String? = null
    var totalVisitCount: String? = null
    var idealVisitCount: String? = null
    var visitSuccessRate: String? = null
    var lastVisitMileage: String? = null
    var lastVisitDate: String? = null
    var nextServiceDueDateForLastVisitDate: String? = null
    var nextServiceDueDateForMileage: String? = null
    var isFollowUpDone: String? = null
    var averageMileage: String? = null
    var lastServiceMileage: String? = null
    var lastServiceType: String? = null
    var lastVisitType: String? = null
    var averageRunning: String? = null
    var serviceNoShowPeriod: String? = null
    var serviceDueBasedonMileage: String? = null
    var serviceDueBasedonTenure: String? = null
    var nextServiceType: String? = null
    var forecastLogic: String? = null
    var variant: String? = null
    var dueDate: String? = null
    var district: String? = null
    var city: String? = null
    var makeCallFrom: String? = null
    var roNumber: String? = null
    var roDate: String? = null
    var salutation: String? = null
    var customerCategory: String? = null
    var attendproperly: String? = null
    var workdoneproperly: String? = null
    var overallinfrastrucure: String? = null
    var workdonexplanation: String? = null
    var washingquality: String? = null
    var overallrating: String? = null
    var customersatisfication: String? = null
    var firebaseKey: String? = null
    var firebaseRef: String? = null
    var coverNoteDate: String? = null
    var nextRenewalDate: String? = null
    private var insuranceCompany: String? = null
    var coverNote: String? = null
    private var lastIDV: String? = null
    var ncBPercentage: String? = null
    var ncBAmount: String? = null
    var odPercentage: String? = null
    var odAmount: String? = null
    var premiumAmount: String? = null
    var serviceLocation: String? = null
    var co_Dealer_Outside: String? = null
    var mileageAtService: String? = null
    var mileage: String? = null
    var reason: String? = null
    var dealerName: String? = null
    var droppedcount: String? = null
    var noncontactautodialDate: String? = null
    var noncontactautodialTime: String? = null
    var reasonForHTML: String? = null
    var dateOfService: String? = null
    var insuredParty: String? = null
    var insuranceDueDate: String? = null
    var alternateMobileno: String? = null
    var alternateVehicleRegno: String? = null
    var uniqueDataCallInit: String? = null
    var booked: String? = null
    var received: String? = null
    var noShow: String? = null
    var totalAssignedCalls: String? = null
    var conversionRate: String? = null
    var pendingCalls: String? = null
    var uniqueidForCallSync = 0
    var serviceBooked: String? = null
    var pickupStarted: String? = null
    var pickupPointReached: String? = null
    var vehiclePicked: String? = null
    var deliveredAtws: String? = null
    var trackStatus: String? = null
    var cancelStatus: String? = null
    var lastDate: String? = null
    var customerCRN: String? = null
    var fuelType: String? = null
    var loyaltyType: String? = null
    var anniversary: String? = null
    var vehicleColor: String? = null
    var pickupOrDrop: String? = null
    var dropStarted: String? = null
    var deliveredToCustomer: String? = null
    var interactionDate: String? = null
    var interactionTime: String? = null
    var userId: String? = null
    var vehicleId: String? = null
    var serviceBookedId: String? = null
    var vehicleRegNo: String? = null
    var custId: String? = null
    var nextServiceDate: String? = null
    var jobCardNumber: String? = null
    var ageOfVehicle = 0
    var daysBetweenVisit = 0
    var chassisNo: String? = null
    var afterServiceSatisfication: String? = null
    var recentServiceSatisfication: String? = null
    var afterServiceComments: String? = null
    var recentServiceComments: String? = null
    var billDate: String? = null
    var workshop: String? = null
    var assignedId: String? = null
    var alreadyservicedatId: String? = null
    var ringingTime = 0.0
    var ringTime: String? = null
    var creName: String? = null
    var policyNo: String? = null
    var lastPremium: String? = null
    var renewalType: String? = null
    var renewalMode: String? = null
    var addOns: String? = null
    var idvPercentage: String? = null
    var premium: String? = null
    var appointmentTime: String? = null
    var paymentType: String? = null
    var paymentReference: String? = null
    var addressReference: String? = null
    var fileSize: String? = null
    var bookingdatetime: String? = null
    var visittype: String? = null

    constructor(parcel: Parcel) : this() {
        callCount = parcel.readString()
        dealerCode = parcel.readString()
        noResponseScheduledCallDate = parcel.readString()
        noServiceReason = parcel.readString()
        followUpDate = parcel.readString()
        followUpTime = parcel.readString()
        pickUpAddress = parcel.readString()
        isFollowupRequired = parcel.readString()
        customerName = parcel.readString()
        currentAddress = parcel.readString()
        appointmentType = parcel.readString()
        permanentAddress = parcel.readString()
        officeAddress = parcel.readString()
        customerEmail = parcel.readString()
        customerPhone = parcel.readString()
        doa = parcel.readString()
        dob = parcel.readString()
        agentName = parcel.readString()
        callType = parcel.readString()
        freeService = parcel.readString()
        lastCallDate = parcel.readString()
        nextCallDate = parcel.readString()
        notes = parcel.readString()
        pickUpRequired = parcel.readString()
        serviceScheduled = parcel.readString()
        serviceScheduledTime = parcel.readString()
        serviceScheduledDate = parcel.readString()
        status = parcel.readString()
        appointmentDate = parcel.readString()
        defectDetails = parcel.readString()
        labourDetails = parcel.readString()
        lastServiceDate = parcel.readString()
        milege = parcel.readString()
        model = parcel.readString()
        nextServiceDue = parcel.readString()
        saleDate = parcel.readString()
        serviceAdvisor = parcel.readString()
        serviceType = parcel.readString()
        technician = parcel.readString()
        vehicleNumber = parcel.readString()
        lastBatteryChangeDate = parcel.readString()
        lastDrumBrakeChange = parcel.readString()
        lastOilChange = parcel.readString()
        vasNotes = parcel.readString()
        filePath = parcel.readString()
        callDate = parcel.readString()
        callTime = parcel.readString()
        callDuration = parcel.readString()
        latitude = parcel.readString()
        longitude = parcel.readString()
        comments = parcel.readString()
        isCallMade = parcel.readString()
        mediaFile = parcel.readString()
        callTypePicId = parcel.readInt()
        vehicalRegNo = parcel.readString()
        totalVisitCount = parcel.readString()
        idealVisitCount = parcel.readString()
        visitSuccessRate = parcel.readString()
        lastVisitMileage = parcel.readString()
        lastVisitDate = parcel.readString()
        nextServiceDueDateForLastVisitDate = parcel.readString()
        nextServiceDueDateForMileage = parcel.readString()
        isFollowUpDone = parcel.readString()
        averageMileage = parcel.readString()
        lastServiceMileage = parcel.readString()
        lastServiceType = parcel.readString()
        lastVisitType = parcel.readString()
        averageRunning = parcel.readString()
        serviceNoShowPeriod = parcel.readString()
        serviceDueBasedonMileage = parcel.readString()
        serviceDueBasedonTenure = parcel.readString()
        nextServiceType = parcel.readString()
        forecastLogic = parcel.readString()
        variant = parcel.readString()
        dueDate = parcel.readString()
        district = parcel.readString()
        city = parcel.readString()
        makeCallFrom = parcel.readString()
        roNumber = parcel.readString()
        roDate = parcel.readString()
        salutation = parcel.readString()
        customerCategory = parcel.readString()
        attendproperly = parcel.readString()
        workdoneproperly = parcel.readString()
        overallinfrastrucure = parcel.readString()
        workdonexplanation = parcel.readString()
        washingquality = parcel.readString()
        overallrating = parcel.readString()
        customersatisfication = parcel.readString()
        firebaseKey = parcel.readString()
        firebaseRef = parcel.readString()
        coverNoteDate = parcel.readString()
        nextRenewalDate = parcel.readString()
        insuranceCompany = parcel.readString()
        coverNote = parcel.readString()
        lastIDV = parcel.readString()
        ncBPercentage = parcel.readString()
        ncBAmount = parcel.readString()
        odPercentage = parcel.readString()
        odAmount = parcel.readString()
        premiumAmount = parcel.readString()
        serviceLocation = parcel.readString()
        co_Dealer_Outside = parcel.readString()
        mileageAtService = parcel.readString()
        mileage = parcel.readString()
        reason = parcel.readString()
        dealerName = parcel.readString()
        droppedcount = parcel.readString()
        noncontactautodialDate = parcel.readString()
        noncontactautodialTime = parcel.readString()
        reasonForHTML = parcel.readString()
        dateOfService = parcel.readString()
        insuredParty = parcel.readString()
        insuranceDueDate = parcel.readString()
        alternateMobileno = parcel.readString()
        alternateVehicleRegno = parcel.readString()
        uniqueDataCallInit = parcel.readString()
        booked = parcel.readString()
        received = parcel.readString()
        noShow = parcel.readString()
        totalAssignedCalls = parcel.readString()
        conversionRate = parcel.readString()
        pendingCalls = parcel.readString()
        uniqueidForCallSync = parcel.readInt()
        serviceBooked = parcel.readString()
        pickupStarted = parcel.readString()
        pickupPointReached = parcel.readString()
        vehiclePicked = parcel.readString()
        deliveredAtws = parcel.readString()
        trackStatus = parcel.readString()
        cancelStatus = parcel.readString()
        lastDate = parcel.readString()
        customerCRN = parcel.readString()
        fuelType = parcel.readString()
        loyaltyType = parcel.readString()
        anniversary = parcel.readString()
        vehicleColor = parcel.readString()
        pickupOrDrop = parcel.readString()
        dropStarted = parcel.readString()
        deliveredToCustomer = parcel.readString()
        interactionDate = parcel.readString()
        interactionTime = parcel.readString()
        userId = parcel.readString()
        vehicleId = parcel.readString()
        serviceBookedId = parcel.readString()
        vehicleRegNo = parcel.readString()
        custId = parcel.readString()
        nextServiceDate = parcel.readString()
        jobCardNumber = parcel.readString()
        ageOfVehicle = parcel.readInt()
        daysBetweenVisit = parcel.readInt()
        chassisNo = parcel.readString()
        afterServiceSatisfication = parcel.readString()
        recentServiceSatisfication = parcel.readString()
        afterServiceComments = parcel.readString()
        recentServiceComments = parcel.readString()
        billDate = parcel.readString()
        workshop = parcel.readString()
        assignedId = parcel.readString()
        alreadyservicedatId = parcel.readString()
        ringingTime = parcel.readDouble()
        ringTime = parcel.readString()
        creName = parcel.readString()
        policyNo = parcel.readString()
        lastPremium = parcel.readString()
        renewalType = parcel.readString()
        renewalMode = parcel.readString()
        addOns = parcel.readString()
        idvPercentage = parcel.readString()
        premium = parcel.readString()
        appointmentTime = parcel.readString()
        paymentType = parcel.readString()
        paymentReference = parcel.readString()
        addressReference = parcel.readString()
        fileSize = parcel.readString()
        bookingdatetime = parcel.readString()
        visittype = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(callCount)
        parcel.writeString(dealerCode)
        parcel.writeString(noResponseScheduledCallDate)
        parcel.writeString(noServiceReason)
        parcel.writeString(followUpDate)
        parcel.writeString(followUpTime)
        parcel.writeString(pickUpAddress)
        parcel.writeString(isFollowupRequired)
        parcel.writeString(customerName)
        parcel.writeString(currentAddress)
        parcel.writeString(appointmentType)
        parcel.writeString(permanentAddress)
        parcel.writeString(officeAddress)
        parcel.writeString(customerEmail)
        parcel.writeString(customerPhone)
        parcel.writeString(doa)
        parcel.writeString(dob)
        parcel.writeString(agentName)
        parcel.writeString(callType)
        parcel.writeString(freeService)
        parcel.writeString(lastCallDate)
        parcel.writeString(nextCallDate)
        parcel.writeString(notes)
        parcel.writeString(pickUpRequired)
        parcel.writeString(serviceScheduled)
        parcel.writeString(serviceScheduledTime)
        parcel.writeString(serviceScheduledDate)
        parcel.writeString(status)
        parcel.writeString(appointmentDate)
        parcel.writeString(defectDetails)
        parcel.writeString(labourDetails)
        parcel.writeString(lastServiceDate)
        parcel.writeString(milege)
        parcel.writeString(model)
        parcel.writeString(nextServiceDue)
        parcel.writeString(saleDate)
        parcel.writeString(serviceAdvisor)
        parcel.writeString(serviceType)
        parcel.writeString(technician)
        parcel.writeString(vehicleNumber)
        parcel.writeString(lastBatteryChangeDate)
        parcel.writeString(lastDrumBrakeChange)
        parcel.writeString(lastOilChange)
        parcel.writeString(vasNotes)
        parcel.writeString(filePath)
        parcel.writeString(callDate)
        parcel.writeString(callTime)
        parcel.writeString(callDuration)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(comments)
        parcel.writeString(isCallMade)
        parcel.writeString(mediaFile)
        parcel.writeInt(callTypePicId)
        parcel.writeString(vehicalRegNo)
        parcel.writeString(totalVisitCount)
        parcel.writeString(idealVisitCount)
        parcel.writeString(visitSuccessRate)
        parcel.writeString(lastVisitMileage)
        parcel.writeString(lastVisitDate)
        parcel.writeString(nextServiceDueDateForLastVisitDate)
        parcel.writeString(nextServiceDueDateForMileage)
        parcel.writeString(isFollowUpDone)
        parcel.writeString(averageMileage)
        parcel.writeString(lastServiceMileage)
        parcel.writeString(lastServiceType)
        parcel.writeString(lastVisitType)
        parcel.writeString(averageRunning)
        parcel.writeString(serviceNoShowPeriod)
        parcel.writeString(serviceDueBasedonMileage)
        parcel.writeString(serviceDueBasedonTenure)
        parcel.writeString(nextServiceType)
        parcel.writeString(forecastLogic)
        parcel.writeString(variant)
        parcel.writeString(dueDate)
        parcel.writeString(district)
        parcel.writeString(city)
        parcel.writeString(makeCallFrom)
        parcel.writeString(roNumber)
        parcel.writeString(roDate)
        parcel.writeString(salutation)
        parcel.writeString(customerCategory)
        parcel.writeString(attendproperly)
        parcel.writeString(workdoneproperly)
        parcel.writeString(overallinfrastrucure)
        parcel.writeString(workdonexplanation)
        parcel.writeString(washingquality)
        parcel.writeString(overallrating)
        parcel.writeString(customersatisfication)
        parcel.writeString(firebaseKey)
        parcel.writeString(firebaseRef)
        parcel.writeString(coverNoteDate)
        parcel.writeString(nextRenewalDate)
        parcel.writeString(insuranceCompany)
        parcel.writeString(coverNote)
        parcel.writeString(lastIDV)
        parcel.writeString(ncBPercentage)
        parcel.writeString(ncBAmount)
        parcel.writeString(odPercentage)
        parcel.writeString(odAmount)
        parcel.writeString(premiumAmount)
        parcel.writeString(serviceLocation)
        parcel.writeString(co_Dealer_Outside)
        parcel.writeString(mileageAtService)
        parcel.writeString(mileage)
        parcel.writeString(reason)
        parcel.writeString(dealerName)
        parcel.writeString(droppedcount)
        parcel.writeString(noncontactautodialDate)
        parcel.writeString(noncontactautodialTime)
        parcel.writeString(reasonForHTML)
        parcel.writeString(dateOfService)
        parcel.writeString(insuredParty)
        parcel.writeString(insuranceDueDate)
        parcel.writeString(alternateMobileno)
        parcel.writeString(alternateVehicleRegno)
        parcel.writeString(uniqueDataCallInit)
        parcel.writeString(booked)
        parcel.writeString(received)
        parcel.writeString(noShow)
        parcel.writeString(totalAssignedCalls)
        parcel.writeString(conversionRate)
        parcel.writeString(pendingCalls)
        parcel.writeInt(uniqueidForCallSync)
        parcel.writeString(serviceBooked)
        parcel.writeString(pickupStarted)
        parcel.writeString(pickupPointReached)
        parcel.writeString(vehiclePicked)
        parcel.writeString(deliveredAtws)
        parcel.writeString(trackStatus)
        parcel.writeString(cancelStatus)
        parcel.writeString(lastDate)
        parcel.writeString(customerCRN)
        parcel.writeString(fuelType)
        parcel.writeString(loyaltyType)
        parcel.writeString(anniversary)
        parcel.writeString(vehicleColor)
        parcel.writeString(pickupOrDrop)
        parcel.writeString(dropStarted)
        parcel.writeString(deliveredToCustomer)
        parcel.writeString(interactionDate)
        parcel.writeString(interactionTime)
        parcel.writeString(userId)
        parcel.writeString(vehicleId)
        parcel.writeString(serviceBookedId)
        parcel.writeString(vehicleRegNo)
        parcel.writeString(custId)
        parcel.writeString(nextServiceDate)
        parcel.writeString(jobCardNumber)
        parcel.writeInt(ageOfVehicle)
        parcel.writeInt(daysBetweenVisit)
        parcel.writeString(chassisNo)
        parcel.writeString(afterServiceSatisfication)
        parcel.writeString(recentServiceSatisfication)
        parcel.writeString(afterServiceComments)
        parcel.writeString(recentServiceComments)
        parcel.writeString(billDate)
        parcel.writeString(workshop)
        parcel.writeString(assignedId)
        parcel.writeString(alreadyservicedatId)
        parcel.writeDouble(ringingTime)
        parcel.writeString(ringTime)
        parcel.writeString(creName)
        parcel.writeString(policyNo)
        parcel.writeString(lastPremium)
        parcel.writeString(renewalType)
        parcel.writeString(renewalMode)
        parcel.writeString(addOns)
        parcel.writeString(idvPercentage)
        parcel.writeString(premium)
        parcel.writeString(appointmentTime)
        parcel.writeString(paymentType)
        parcel.writeString(paymentReference)
        parcel.writeString(addressReference)
        parcel.writeString(fileSize)
        parcel.writeString(bookingdatetime)
        parcel.writeString(visittype)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<CallInfo> {
        override fun createFromParcel(parcel: Parcel): CallInfo {
            return CallInfo(parcel)
        }

        override fun newArray(size: Int): Array<CallInfo?> {
            return arrayOfNulls(size)
        }
    }


}